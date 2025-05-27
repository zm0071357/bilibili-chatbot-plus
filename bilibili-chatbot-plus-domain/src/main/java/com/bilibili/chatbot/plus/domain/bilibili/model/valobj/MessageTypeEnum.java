package com.bilibili.chatbot.plus.domain.bilibili.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 消息类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum MessageTypeEnum {

    TEXT(1, "文字消息"),
    IMAGE(2, "图片消息"),
    CUSTOM_EMOJI(6, "自定义表情消息"),
    SHARE(7, "分享消息"),
    NOTIFY(10, "通知消息"),
    ;
    private Integer type;
    private String info;
}
