package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMessageResponseEntity {
    private Integer code;
    private String message;
    private Integer ttl;
    private Data data;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private long msg_key;
        private String msg_content;
    }
}
