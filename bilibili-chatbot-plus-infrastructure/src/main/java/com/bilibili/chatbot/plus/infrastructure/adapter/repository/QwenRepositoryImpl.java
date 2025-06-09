package com.bilibili.chatbot.plus.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.AnalysisVideoEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.MessageContextEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.ShareContent;
import com.bilibili.chatbot.plus.domain.qwen.adapter.port.VideoPort;
import com.bilibili.chatbot.plus.domain.qwen.adapter.repository.QwenRepository;
import com.bilibili.chatbot.plus.domain.qwen.model.entity.QwenResponseEntity;
import com.bilibili.chatbot.plus.domain.qwen.model.valobj.RoleConstant;
import lombok.extern.slf4j.Slf4j;
import qwen.sdk.largemodel.chat.enums.ChatModelEnum;
import qwen.sdk.largemodel.chat.impl.ChatServiceImpl;
import qwen.sdk.largemodel.chat.model.ChatMutiResponse;
import qwen.sdk.largemodel.chat.model.ChatRequest;
import qwen.sdk.largemodel.image.enums.FunctionEnum;
import qwen.sdk.largemodel.image.enums.ImageEnum;
import qwen.sdk.largemodel.image.enums.ImageTaskStatusEnum;
import qwen.sdk.largemodel.image.enums.SizeEnum;
import qwen.sdk.largemodel.image.impl.ImageServiceImpl;
import qwen.sdk.largemodel.image.model.ImageRequest;
import qwen.sdk.largemodel.image.model.ImageResponse;
import qwen.sdk.largemodel.image.model.ResultResponse;
import qwen.sdk.largemodel.video.enums.VideoModelEnum;
import qwen.sdk.largemodel.video.enums.VideoTaskStatusEnum;
import qwen.sdk.largemodel.video.impl.VideoServiceImpl;
import qwen.sdk.largemodel.video.model.VideoRequest;
import qwen.sdk.largemodel.video.model.VideoResponse;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class QwenRepositoryImpl implements QwenRepository {

    private final VideoPort videoPort;

    private final ChatServiceImpl chatServiceImpl;

    private final ImageServiceImpl imageServiceImpl;

    private final VideoServiceImpl videoServiceImpl;

    public Map<Long, List<ChatRequest.Input.Message>> history = new ConcurrentHashMap<>();

    public QwenRepositoryImpl(VideoPort videoPort, ChatServiceImpl chatServiceImpl, ImageServiceImpl imageServiceImpl, VideoServiceImpl videoServiceImpl) {
        this.videoPort = videoPort;
        this.chatServiceImpl = chatServiceImpl;
        this.imageServiceImpl = imageServiceImpl;
        this.videoServiceImpl = videoServiceImpl;
    }

    @Override
    public QwenResponseEntity handleTextMessage(MessageContextEntity entity) {
        long senderUid = entity.getSenderUid();
        String question = entity.getQuestion();
        List<ChatRequest.Input.Message> messages = getHistory(senderUid);
        List<ChatRequest.Input.Message.Content> userContent = new ArrayList<>();
        userContent.add(ChatRequest.Input.Message.Content.builder()
                .text(question)
                .build());
        messages.add(ChatRequest.Input.Message.builder()
                .role(RoleConstant.USER)
                .content(userContent)
                .build());
        ChatRequest request = ChatRequest.builder()
                .model(ChatModelEnum.QWEN_VL_MAX_LATEST.getModel())
                .input(ChatRequest.Input.builder()
                        .messages(messages)
                        .build())
                .parameters(ChatRequest.Parameters.builder()
                        .resultFormat("message")
                        .enableSearch(true)
                        .searchOptions(ChatRequest.Parameters.SearchOptions.builder()
                                .enableSource(true)
                                .forcedSearch(true)
                                .build())
                        .build())
                .build();
        log.info("请求参数:{}", JSON.toJSONString(request));
        try {
            // 发起请求
            ChatMutiResponse response = chatServiceImpl.chatWithMultimodal(request);
            String result = String.valueOf(response.getOutput().getChoices().get(0).getMessage().getContent().get(0).getText());
            log.info("返回结果:{}", JSON.toJSONString(response));
            // 添加历史记录
            messages.add(ChatRequest.Input.Message.builder()
                    .role(RoleConstant.SYSTEM)
                    .content(result)
                    .build());
            // 更新
            history.put(senderUid, messages);
            return QwenResponseEntity.builder()
                    .result(result)
                    .isText(true)
                    .build();
        } catch (IOException e) {
            messages.remove(messages.size() - 1);
            // 更新
            history.put(senderUid, messages);
            return QwenResponseEntity.builder()
                    .result(MessageConstant.TEXT_FAILED_MESSAGE)
                    .isText(true)
                    .build();
        }
    }

    @Override
    public QwenResponseEntity createImage(String request) throws IOException {
        ImageRequest imageRequest = ImageRequest.builder()
                .model(ImageEnum.WANX_21_T2I_TURBO.getModel())
                .input(ImageRequest.Input.builder().prompt(request).build())
                .parameters(ImageRequest.Parameters.builder()
                        .size(SizeEnum.ONE_ONE.getResolution())
                        .n(1)
                        .build())
                .build();
        ImageResponse response = imageServiceImpl.imageSynthesis(imageRequest);
        String taskId = response.getOutput().getTask_id();
        String curStatus = ImageTaskStatusEnum.RUNNING.getCode();
        ResultResponse result = null;
        int count = 0;
        int maxCount = 150;
        // 轮询获取任务结果
        while (count < maxCount || ImageTaskStatusEnum.RUNNING.getCode().equals(curStatus)) {
            result = imageServiceImpl.result(taskId);
            curStatus = result.getOutput().getTask_status();
            count += 1;
            log.info("请求次数:{},结果:{}", count, curStatus);
            if (ImageTaskStatusEnum.SUCCEEDED.getCode().equals(curStatus)) {
                break;
            } else if (ImageTaskStatusEnum.FAILED.getCode().equals(curStatus) || ImageTaskStatusEnum.UNKNOWN.getCode().equals(curStatus)) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
        }
        String url = result.getOutput().getResults().get(0).getUrl();
        log.info("url:{}", url);
        return QwenResponseEntity.builder()
                .result(url)
                .isImage(true)
                .build();
    }

    @Override
    public QwenResponseEntity descriptionEdit(String request, String url) throws IOException {
        ImageRequest imageRequest = ImageRequest.builder()
                .model(ImageEnum.WANX_21_T2I_TURBO.getModel())
                .input(ImageRequest.InputExtend.builder()
                        .function(FunctionEnum.DESCRIPTION_EDIT.getFunction())
                        .prompt(request)
                        .base_image_url(url)
                        .build())
                .parameters(ImageRequest.Parameters.builder()
                        .size(SizeEnum.ONE_ONE.getResolution())
                        .n(1)
                        .build())
                .build();
        ImageResponse response = imageServiceImpl.imageSynthesis(imageRequest);
        String taskId = response.getOutput().getTask_id();
        String curStatus = ImageTaskStatusEnum.RUNNING.getCode();
        ResultResponse result = null;
        int count = 0;
        int maxCount = 150;
        // 轮询获取任务结果
        while (count < maxCount || ImageTaskStatusEnum.RUNNING.getCode().equals(curStatus)) {
            result = imageServiceImpl.result(taskId);
            curStatus = result.getOutput().getTask_status();
            count += 1;
            log.info("请求次数:{},结果:{}", count, curStatus);
            if (ImageTaskStatusEnum.SUCCEEDED.getCode().equals(curStatus)) {
                break;
            } else if (ImageTaskStatusEnum.FAILED.getCode().equals(curStatus) || ImageTaskStatusEnum.UNKNOWN.getCode().equals(curStatus)) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
        }
        String newUrl = result.getOutput().getResults().get(0).getUrl();
        log.info("url:{}", newUrl);
        return QwenResponseEntity.builder()
                .result(newUrl)
                .isImage(true)
                .build();
    }

    @Override
    public QwenResponseEntity removeWatermark(String request, String url) throws IOException {
        ImageRequest imageRequest = ImageRequest.builder()
                .model(ImageEnum.WANX_21_T2I_TURBO.getModel())
                .input(ImageRequest.InputExtend.builder()
                        .function(FunctionEnum.REMOVE_WATERMARK.getFunction())
                        .prompt(request)
                        .base_image_url(url)
                        .build())
                .parameters(ImageRequest.Parameters.builder()
                        .size(SizeEnum.ONE_ONE.getResolution())
                        .n(1)
                        .build())
                .build();
        ImageResponse response = imageServiceImpl.imageSynthesis(imageRequest);
        String taskId = response.getOutput().getTask_id();
        String curStatus = ImageTaskStatusEnum.RUNNING.getCode();
        ResultResponse result = null;
        int count = 0;
        int maxCount = 150;
        // 轮询获取任务结果
        while (count < maxCount || ImageTaskStatusEnum.RUNNING.getCode().equals(curStatus)) {
            result = imageServiceImpl.result(taskId);
            curStatus = result.getOutput().getTask_status();
            count += 1;
            log.info("请求次数:{},结果:{}", count, curStatus);
            if (ImageTaskStatusEnum.SUCCEEDED.getCode().equals(curStatus)) {
                break;
            } else if (ImageTaskStatusEnum.FAILED.getCode().equals(curStatus) || ImageTaskStatusEnum.UNKNOWN.getCode().equals(curStatus)) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
        }
        String newUrl = result.getOutput().getResults().get(0).getUrl();
        log.info("url:{}", newUrl);
        return QwenResponseEntity.builder()
                .result(newUrl)
                .isImage(true)
                .build();
    }

    @Override
    public QwenResponseEntity expand(String request, String url) throws IOException {
        ImageRequest imageRequest = ImageRequest.builder()
                .model(ImageEnum.WANX_21_T2I_TURBO.getModel())
                .input(ImageRequest.InputExtend.builder()
                        .function(FunctionEnum.EXPAND.getFunction())
                        .prompt(request)
                        .base_image_url(url)
                        .build())
                .parameters(ImageRequest.ParametersExtend.builder()
                        .size(SizeEnum.ONE_ONE.getResolution())
                        .top_scale(1.5F)
                        .bottom_scale(1.5F)
                        .left_scale(1.5F)
                        .right_scale(1.5F)
                        .n(1)
                        .build())
                .build();
        ImageResponse response = imageServiceImpl.imageSynthesis(imageRequest);
        String taskId = response.getOutput().getTask_id();
        String curStatus = ImageTaskStatusEnum.RUNNING.getCode();
        ResultResponse result = null;
        int count = 0;
        int maxCount = 150;
        // 轮询获取任务结果
        while (count < maxCount || ImageTaskStatusEnum.RUNNING.getCode().equals(curStatus)) {
            result = imageServiceImpl.result(taskId);
            curStatus = result.getOutput().getTask_status();
            count += 1;
            log.info("请求次数:{},结果:{}", count, curStatus);
            if (ImageTaskStatusEnum.SUCCEEDED.getCode().equals(curStatus)) {
                break;
            } else if (ImageTaskStatusEnum.FAILED.getCode().equals(curStatus) || ImageTaskStatusEnum.UNKNOWN.getCode().equals(curStatus)) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
        }
        String newUrl = result.getOutput().getResults().get(0).getUrl();
        log.info("url:{}", newUrl);
        return QwenResponseEntity.builder()
                .result(newUrl)
                .isImage(true)
                .build();
    }

    @Override
    public QwenResponseEntity superResolution(String request, String url) throws IOException {
        ImageRequest imageRequest = ImageRequest.builder()
                .model(ImageEnum.WANX_21_T2I_TURBO.getModel())
                .input(ImageRequest.InputExtend.builder()
                        .function(FunctionEnum.SUPER_RESOLUTION.getFunction())
                        .prompt(request)
                        .base_image_url(url)
                        .build())
                .parameters(ImageRequest.Parameters.builder()
                        .size(SizeEnum.ONE_ONE.getResolution())
                        .n(1)
                        .build())
                .build();
        ImageResponse response = imageServiceImpl.imageSynthesis(imageRequest);
        String taskId = response.getOutput().getTask_id();
        String curStatus = ImageTaskStatusEnum.RUNNING.getCode();
        ResultResponse result = null;
        int count = 0;
        int maxCount = 150;
        // 轮询获取任务结果
        while (count < maxCount || ImageTaskStatusEnum.RUNNING.getCode().equals(curStatus)) {
            result = imageServiceImpl.result(taskId);
            curStatus = result.getOutput().getTask_status();
            count += 1;
            log.info("请求次数:{},结果:{}", count, curStatus);
            if (ImageTaskStatusEnum.SUCCEEDED.getCode().equals(curStatus)) {
                break;
            } else if (ImageTaskStatusEnum.FAILED.getCode().equals(curStatus) || ImageTaskStatusEnum.UNKNOWN.getCode().equals(curStatus)) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
        }
        String newUrl = result.getOutput().getResults().get(0).getUrl();
        log.info("url:{}", newUrl);
        return QwenResponseEntity.builder()
                .result(newUrl)
                .isImage(true)
                .build();
    }

    @Override
    public QwenResponseEntity colorization(String request, String url) throws IOException {
        ImageRequest imageRequest = ImageRequest.builder()
                .model(ImageEnum.WANX_21_T2I_TURBO.getModel())
                .input(ImageRequest.InputExtend.builder()
                        .function(FunctionEnum.COLORIZATION.getFunction())
                        .prompt(request)
                        .base_image_url(url)
                        .build())
                .parameters(ImageRequest.Parameters.builder()
                        .size(SizeEnum.ONE_ONE.getResolution())
                        .n(1)
                        .build())
                .build();
        ImageResponse response = imageServiceImpl.imageSynthesis(imageRequest);
        String taskId = response.getOutput().getTask_id();
        String curStatus = ImageTaskStatusEnum.RUNNING.getCode();
        ResultResponse result = null;
        int count = 0;
        int maxCount = 150;
        // 轮询获取任务结果
        while (count < maxCount || ImageTaskStatusEnum.RUNNING.getCode().equals(curStatus)) {
            result = imageServiceImpl.result(taskId);
            curStatus = result.getOutput().getTask_status();
            count += 1;
            log.info("请求次数:{},结果:{}", count, curStatus);
            if (ImageTaskStatusEnum.SUCCEEDED.getCode().equals(curStatus)) {
                break;
            } else if (ImageTaskStatusEnum.FAILED.getCode().equals(curStatus) || ImageTaskStatusEnum.UNKNOWN.getCode().equals(curStatus)) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.IMAGE_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
        }
        String newUrl = result.getOutput().getResults().get(0).getUrl();
        log.info("url:{}", newUrl);
        return QwenResponseEntity.builder()
                .result(newUrl)
                .isImage(true)
                .build();
    }

    @Override
    public QwenResponseEntity createVideo(String request) throws IOException {
        VideoRequest videoRequest = VideoRequest.builder()
                .model(VideoModelEnum.WANX_21_T2V_TURBO.getModel())
                .input(VideoRequest.Input.builder()
                        .prompt(request)
                        .build())
                .parameters(VideoRequest.ParametersExtend.builder()
                        .promptExtend(true)
                        .build())
                .build();
        VideoResponse response = videoServiceImpl.videoSynthesis(videoRequest);
        String taskId = response.getOutput().getTask_id();
        String curStatus = ImageTaskStatusEnum.RUNNING.getCode();
        qwen.sdk.largemodel.video.model.ResultResponse result = null;
        int count = 0;
        int maxCount = 150;
        // 轮询获取任务结果
        while (count < maxCount || VideoTaskStatusEnum.RUNNING.getCode().equals(curStatus)) {
            result = videoServiceImpl.result(taskId);
            curStatus = result.getOutput().getTask_status();
            count += 1;
            log.info("请求次数:{},结果:{}", count, curStatus);
            if (VideoTaskStatusEnum.SUCCEEDED.getCode().equals(curStatus)) {
                break;
            } else if (VideoTaskStatusEnum.FAILED.getCode().equals(curStatus) || VideoTaskStatusEnum.UNKNOWN.getCode().equals(curStatus)) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.VIDEO_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.VIDEO_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
        }
        String url = result.getOutput().getVideo_url();
        log.info("url:{}", url);
        return QwenResponseEntity.builder()
                .result(url)
                .isVideo(true)
                .build();
    }

    @Override
    public QwenResponseEntity createVideoWithBaseImageUrl(String request, String url) throws IOException {
        VideoRequest videoRequest = VideoRequest.builder()
                .model(VideoModelEnum.WANX_21_T2V_TURBO.getModel())
                .input(VideoRequest.Input.builder()
                        .imgUrl(url)
                        .prompt(request)
                        .build())
                .parameters(VideoRequest.Parameters.builder()
                        .promptExtend(true)
                        .build())
                .build();
        VideoResponse response = videoServiceImpl.videoSynthesis(videoRequest);
        String taskId = response.getOutput().getTask_id();
        String curStatus = ImageTaskStatusEnum.RUNNING.getCode();
        qwen.sdk.largemodel.video.model.ResultResponse result = null;
        int count = 0;
        int maxCount = 150;
        // 轮询获取任务结果
        while (count < maxCount || VideoTaskStatusEnum.RUNNING.getCode().equals(curStatus)) {
            result = videoServiceImpl.result(taskId);
            curStatus = result.getOutput().getTask_status();
            count += 1;
            log.info("请求次数:{},结果:{}", count, curStatus);
            if (VideoTaskStatusEnum.SUCCEEDED.getCode().equals(curStatus)) {
                break;
            } else if (VideoTaskStatusEnum.FAILED.getCode().equals(curStatus) || VideoTaskStatusEnum.UNKNOWN.getCode().equals(curStatus)) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.VIDEO_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                return QwenResponseEntity.builder()
                        .result(MessageConstant.VIDEO_FAILED_MESSAGE)
                        .isText(true)
                        .build();
            }
        }
        String newUrl = result.getOutput().getVideo_url();
        log.info("url:{}", newUrl);
        return QwenResponseEntity.builder()
                .result(newUrl)
                .isVideo(true)
                .build();
    }

    @Override
    public String getOnlineLink(String url) throws IOException {
        Call<AnalysisVideoEntity> call = videoPort.analysis(url);
        Response<AnalysisVideoEntity> response = call.execute();
        AnalysisVideoEntity analysisVideoEntity = response.body();
        return analysisVideoEntity.getData().getUrl();
    }

    @Override
    public QwenResponseEntity handleImageMessage(MessageContextEntity entity) {
        long senderUid = entity.getSenderUid();
        String imageUrl = entity.getQuestion();
        List<ChatRequest.Input.Message> messages = getHistory(senderUid);
        List<ChatRequest.Input.Message.Content> userContent = new ArrayList<>();
        userContent.add(ChatRequest.Input.Message.Content.builder()
                    .image(imageUrl)
                    .build());
        userContent.add(ChatRequest.Input.Message.Content.builder()
                    .text(MessageConstant.DES_IMAGE_MESSAGE)
                    .build());
        messages.add(ChatRequest.Input.Message.builder()
                .role(RoleConstant.USER)
                .content(userContent)
                .build());
        ChatRequest request = ChatRequest.builder()
                .model(ChatModelEnum.QWEN_VL_MAX_LATEST.getModel())
                .input(ChatRequest.Input.builder()
                        .messages(messages)
                        .build())
                .parameters(ChatRequest.Parameters.builder()
                        .resultFormat("message")
//                        .enableSearch(true)
//                        .searchOptions(ChatRequest.Parameters.SearchOptions.builder()
//                                .enableSource(true)
//                                .forcedSearch(true)
//                                .build())
                        .build())
                .build();
        log.info("请求参数:{}", JSON.toJSONString(request));
        // 发起请求
        try {
            ChatMutiResponse response = chatServiceImpl.chatWithMultimodal(request);
            String result = String.valueOf(response.getOutput().getChoices().get(0).getMessage().getContent().get(0).getText());
            log.info("返回结果:{}", JSON.toJSONString(response));
            // 添加历史记录
            messages.add(ChatRequest.Input.Message.builder()
                    .role(RoleConstant.SYSTEM)
                    .content(result)
                    .build());
            // 更新
            history.put(senderUid, messages);
            return QwenResponseEntity.builder()
                    .result(result)
                    .isText(true)
                    .build();
        } catch (IOException e) {
            messages.remove(messages.size() - 1);
            // 更新
            history.put(senderUid, messages);
            return QwenResponseEntity.builder()
                    .result(MessageConstant.TEXT_FAILED_MESSAGE)
                    .isText(true)
                    .build();
        }

    }

    @Override
    public QwenResponseEntity handleVideoMessage(MessageContextEntity entity) {
        ShareContent shareContent = entity.getShareContent();
        long senderUid = entity.getSenderUid();
        List<ChatRequest.Input.Message> messages = getHistory(senderUid);
        try {
            String url = this.getOnlineLink(shareContent.getVideoUrl(shareContent.getBvid()));
            log.info("url解析结果:{}", url);
            List<ChatRequest.Input.Message.Content> userContent = new ArrayList<>();
            userContent.add(ChatRequest.Input.Message.Content.builder()
                    .video(url)
                    .build());
            userContent.add(ChatRequest.Input.Message.Content.builder()
                    .image(shareContent.getThumb())
                    .build());
            userContent.add(ChatRequest.Input.Message.Content.builder()
                    .text(MessageConstant.DES_VIDEO_MESSAGE)
                    .build());
            messages.add(ChatRequest.Input.Message.builder()
                    .role(RoleConstant.USER)
                    .content(userContent)
                    .build());

            ChatRequest request = ChatRequest.builder()
                    .model(ChatModelEnum.QWEN_VL_PLUS.getModel())
                    .input(ChatRequest.Input.builder()
                            .messages(messages)
                            .build())
                    .parameters(ChatRequest.Parameters.builder()
                            .resultFormat("message")
                            .enableSearch(true)
                            .searchOptions(ChatRequest.Parameters.SearchOptions.builder()
                                    .enableSource(true)
                                    .forcedSearch(true)
                                    .build())
                            .build())
                    .build();
            log.info("请求参数:{}", JSON.toJSONString(request));
            // 发起请求
            ChatMutiResponse response = chatServiceImpl.chatWithMultimodal(request);
            String result = String.valueOf(response.getOutput().getChoices().get(0).getMessage().getContent().get(0).getText());
            log.info("返回结果:{}", JSON.toJSONString(response));
            // 添加历史记录
            messages.add(ChatRequest.Input.Message.builder()
                    .role(RoleConstant.SYSTEM)
                    .content(result)
                    .build());
            messages.remove(messages.size() - 1);
            messages.remove(messages.size() - 1);
            // 更新
            history.put(senderUid, messages);
            return QwenResponseEntity.builder()
                    .result(result)
                    .isText(true)
                    .build();
        } catch (IOException e) {
            messages.remove(messages.size() - 1);
            // 更新
            history.put(senderUid, messages);
            return QwenResponseEntity.builder()
                    .result(MessageConstant.SHARE_FAILED_MESSAGE)
                    .isText(true)
                    .build();
        }

    }

    /**
     * 根据用户ID获取历史记录
     * @param userId 用户ID
     * @return 历史记录消息集合
     */
    private List<ChatRequest.Input.Message> getHistory(long userId) {
        return history.computeIfAbsent(userId, this::defaultHistory);
    }

    /**
     * 创建初始记录
     * @param userId 用户ID
     * @return 初始消息集合
     */
    private List<ChatRequest.Input.Message> defaultHistory(long userId) {
        log.info("用户初次对话，创建历史记录:{}", userId);
        List<ChatRequest.Input.Message> messages = new ArrayList<>();
        List<ChatRequest.Input.Message.Content> systemContent = new ArrayList<>();
        systemContent.add(ChatRequest.Input.Message.Content.builder().text(MessageConstant.DEFAULT_MESSAGE).build());
        messages.add(ChatRequest.Input.Message.builder()
                .role(RoleConstant.SYSTEM)
                .content(systemContent)
                .build());
        return messages;
    }

    /**
     * 返回整个历史记录集合
     * @return
     */
    public Map<Long, List<ChatRequest.Input.Message>> getHistory() {
        return history;
    }
}
