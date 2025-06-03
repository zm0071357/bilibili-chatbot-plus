package com.bilibili.chatbot.plus.domain.qwen;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.MessageContextEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageTypeEnum;
import com.bilibili.chatbot.plus.domain.qwen.model.entity.QwenResponseEntity;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 通义千问大模型抽象类
 */
@Slf4j
public abstract class AbstractQwenService implements QwenService {

    // 消息类型处理器映射
    private final Map<Integer, Function<MessageContextEntity, QwenResponseEntity>> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 初始化处理器映射
        handlerMap.put(MessageTypeEnum.TEXT.getType(), this::handleTextMessage);
        handlerMap.put(MessageTypeEnum.IMAGE.getType(), this::handleImageMessage);
        handlerMap.put(MessageTypeEnum.CUSTOM_EMOJI.getType(), this::handleImageMessage);
        handlerMap.put(MessageTypeEnum.SHARE.getType(), this::handleVideoMessage);
        handlerMap.put(MessageTypeEnum.NOTIFY.getType(), this::handleDefaultMessage);
        log.info("消息处理器初始化完成");
    }

    @Override
    public QwenResponseEntity handle(MessageContextEntity messageContextEntity) {
        Integer msgType = messageContextEntity.getMsgType();
        return handlerMap.get(msgType).apply(messageContextEntity);
    }

    // 文字消息处理
    protected abstract QwenResponseEntity handleTextMessage(MessageContextEntity entity);

    // 图片消息处理
    protected abstract QwenResponseEntity handleImageMessage(MessageContextEntity entity);

    // 视频消息处理
    protected abstract QwenResponseEntity handleVideoMessage(MessageContextEntity entity);

    // 默认消息处理
    protected QwenResponseEntity handleDefaultMessage(MessageContextEntity entity) {
        return QwenResponseEntity.builder()
                .result(MessageConstant.NOTIFY_MESSAGE)
                .isImage(false)
                .build();
    }
}
