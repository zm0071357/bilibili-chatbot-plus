package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 上传封面图结果实体
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendCoverResponseEntity {

    private Integer code;
    private String message;
    private Integer ttl;
    private Data data;

    @Getter
    public static class Data {
        private String url;
    }
}

