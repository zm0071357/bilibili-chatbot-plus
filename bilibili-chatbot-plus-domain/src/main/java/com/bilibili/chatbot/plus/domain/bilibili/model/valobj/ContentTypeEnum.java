package com.bilibili.chatbot.plus.domain.bilibili.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 内容类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ContentTypeEnum {

    TEXT("content", "TEXT", "文字消息"),
    IMAGE("url", "IMAGE", "图片消息"),
    SHARE("bvid", "SHARE", "分享消息")
    ;

    private String type;
    private String sign;
    private String info;
}
