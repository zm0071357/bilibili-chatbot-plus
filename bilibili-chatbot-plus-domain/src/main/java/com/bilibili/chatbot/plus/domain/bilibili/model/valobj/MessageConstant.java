package com.bilibili.chatbot.plus.domain.bilibili.model.valobj;

import com.alibaba.fastjson.JSON;


public class MessageConstant {
    public static final String DEFAULT_MESSAGE = "你是ZM创造出来的一个AI助手，你叫林薯条，是因为ZM很喜欢吃薯条，说话用朋友的口吻";
    public static final String PRE_MESSAGE = "薯条正在思考~";
    public static final String DES_IMAGE_MESSAGE = "仔细描述这张图片";
    public static final String DES_VIDEO_MESSAGE = "根据封面图和视频内容总结这个视频";
    public static final String NOTIFY_MESSAGE = "薯条回复了通知消息~";
    public static final String UNKNOWN_MESSAGE = "薯条回复了未知消息~";
    public static final String TEXT_FAILED_MESSAGE = "回复失败，请稍后再试~";
    public static final String IMAGE_FAILED_MESSAGE = "生成失败，请稍后再试~";
    public static final String SHARE_FAILED_MESSAGE = "总结失败，请稍后再试~";
    public static final String NO_REFER_IMAGE = "请先提供一张参考图";
    public static final String BIZ = "im";

    public static String getContent(String content) {
        return JSON.toJSONString(TextContent.builder().content(content).build());
    }

    public static String getImageContent(String url, Integer height, Integer width, Double size) {
        return JSON.toJSONString(ImageContent.builder()
                .url(url)
                .height(height)
                .width(width)
                .size(size)
                .build());
    }

}
