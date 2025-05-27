package com.bilibili.chatbot.plus.domain.bilibili.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 接收者类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ReceiverTypeEnum {

    USER(1, "用户"),
    FANS(2, "粉丝团"),
    ;
    private Integer type;
    private String info;
}
