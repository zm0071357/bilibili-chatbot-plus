package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponseEntity {
    private Integer code;
    private String message;
    private Integer ttl;
}
