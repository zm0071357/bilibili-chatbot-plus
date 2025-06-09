package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import lombok.*;

/**
 * 上传视频结果实体
 */
@Getter
@Builder
public class SubmitVideoResponseEntity {
    private String result;
    private boolean isSuccess;
}
