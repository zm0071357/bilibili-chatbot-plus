package com.bilibili.chatbot.plus.domain.qwen.service;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.MessageContextEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import com.bilibili.chatbot.plus.domain.qwen.AbstractQwenService;
import com.bilibili.chatbot.plus.domain.qwen.adapter.repository.QwenRepository;
import com.bilibili.chatbot.plus.domain.qwen.model.entity.CommandRequestEntity;
import com.bilibili.chatbot.plus.domain.qwen.model.entity.QwenResponseEntity;
import com.bilibili.chatbot.plus.domain.qwen.model.valobj.CommandEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
@Slf4j
public class QwenServiceImpl extends AbstractQwenService{

    @Resource
    private QwenRepository qwenRepository;

    private final Map<CommandEnum, Function<CommandRequestEntity, QwenResponseEntity>> commandHandleMap = new HashMap<>();

    private Map<Long, String> lastImageMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        super.init();
        commandHandleMap.put(CommandEnum.CREATE_IMAGE, this::createImage);
        commandHandleMap.put(CommandEnum.DESCRIPTION_EDIT, this::descriptionEdit);
        commandHandleMap.put(CommandEnum.REMOVE_WATERMARK, this::removeWatermark);
        commandHandleMap.put(CommandEnum.EXPAND, this::expand);
        commandHandleMap.put(CommandEnum.SUPER_RESOLUTION, this::superResolution);
        commandHandleMap.put(CommandEnum.COLORIZATION, this::colorization);
        log.info("命令处理器初始化完成");
    }

    @Override
    protected QwenResponseEntity handleTextMessage(MessageContextEntity entity) {
        String question = entity.getQuestion();
        long senderUid = entity.getSenderUid();
        CommandEnum command = CommandEnum.getCommand(question);
        if (command == null) {
            log.info("普通文字消息:{},{}", senderUid, question);
            return qwenRepository.handleTextMessage(entity);
        } else {
            Function<CommandRequestEntity, QwenResponseEntity> handler = commandHandleMap.get(command);
            return handler.apply(CommandRequestEntity.builder()
                    .senderUid(senderUid)
                    .question(question)
                    .commandEnum(command)
                    .build());
        }
    }

    private QwenResponseEntity createImage(CommandRequestEntity commandRequestEntity) {
        try {
            long senderUid = commandRequestEntity.getSenderUid();
            String question = commandRequestEntity.getQuestion();
            String request = commandRequestEntity.getRequest(question);
            QwenResponseEntity response = qwenRepository.createImage(request);
            String url = String.valueOf(response.getResult());
            lastImageMap.put(senderUid, url);
            log.info("指令:{}.要求:{},生成的图片url:{}",commandRequestEntity.getCommandEnum().getCommand(), request, url);
            return response;
        } catch (IOException e) {
            return QwenResponseEntity.builder()
                    .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                    .isImage(false)
                    .build();
        }
    }

    private QwenResponseEntity descriptionEdit(CommandRequestEntity commandRequestEntity) {
        return commonImageProcessing(commandRequestEntity, (request, url) -> {
            try {
                return qwenRepository.descriptionEdit(request, url);
            } catch (IOException e) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isImage(false)
                        .build();
            }
        });
    }

    private QwenResponseEntity removeWatermark(CommandRequestEntity commandRequestEntity) {
        return commonImageProcessing(commandRequestEntity, (request, url) -> {
            try {
                return qwenRepository.removeWatermark(request, url);
            } catch (IOException e) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isImage(false)
                        .build();
            }
        });
    }

    private QwenResponseEntity expand(CommandRequestEntity commandRequestEntity) {
        return commonImageProcessing(commandRequestEntity, (request, url) -> {
            try {
                return qwenRepository.expand(request, url);
            } catch (IOException e) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isImage(false)
                        .build();
            }
        });
    }

    private QwenResponseEntity superResolution(CommandRequestEntity commandRequestEntity) {
        return commonImageProcessing(commandRequestEntity, (request, url) -> {
            try {
                return qwenRepository.superResolution(request, url);
            } catch (IOException e) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isImage(false)
                        .build();
            }
        });
    }

    private QwenResponseEntity colorization(CommandRequestEntity commandRequestEntity) {
        return commonImageProcessing(commandRequestEntity, (request, url) -> {
            try {
                return qwenRepository.colorization(request, url);
            } catch (IOException e) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isImage(false)
                        .build();
            }
        });
    }

    /**
     * 图像编辑通用处理方法
     * @return
     */
    private QwenResponseEntity commonImageProcessing(
            CommandRequestEntity commandRequestEntity,
            BiFunction<String, String, QwenResponseEntity> imageProcessor // 策略接口
    ) {
            long senderUid = commandRequestEntity.getSenderUid();
            String url = lastImageMap.get(senderUid);
            if (url == null) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.NO_REFER_IMAGE)
                        .isImage(false)
                        .build();
            }
            String question = commandRequestEntity.getQuestion();
            String request = commandRequestEntity.getRequest(question);
            // 执行图像处理策略（由外部传入具体实现）
            QwenResponseEntity response = imageProcessor.apply(request, url);
            String newUrl = String.valueOf(response.getResult());
            lastImageMap.put(senderUid, newUrl);
            log.info("指令:{}.要求:{},生成的图片url:{}",
                    commandRequestEntity.getCommandEnum().getCommand(),
                    request,
                    newUrl);
            return response;
    }

    @Override
    protected QwenResponseEntity handleImageMessage(MessageContextEntity entity) {
        lastImageMap.put(entity.getSenderUid(), entity.getQuestion());
        return qwenRepository.handleImageMessage(entity);
    }

    @Override
    protected QwenResponseEntity handleVideoMessage(MessageContextEntity entity){
        return qwenRepository.handleVideoMessage(entity);
    }
}
