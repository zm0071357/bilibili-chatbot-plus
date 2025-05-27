package com.bilibili.chatbot.plus.domain.bilibili.model.valobj;

public class MessageConstant {
    public static final String TEXT_MESSAGE = "薯条正在思考~";
    public static final String IMAGE_MESSAGE = "薯条正在识别~";
    public static final String SHARE_MESSAGE = "薯条正在总结~";
    public static final String NOTIFY_MESSAGE = "薯条回复了通知消息~";
    public static final String UNKNOWN_MESSAGE = "薯条回复了未知消息~";

    public static String getContent(String content) {
        return "{\"content\":\"" + content + "\"}";
    }

}
