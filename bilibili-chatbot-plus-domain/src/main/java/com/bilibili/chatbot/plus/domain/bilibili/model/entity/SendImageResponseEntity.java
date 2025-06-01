package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendImageResponseEntity {

    private Integer code;
    private String message;
    private Integer ttl;
    private Data data;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        @JsonProperty("image_height")
        private Integer imageHeight;
        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("image_width")
        private Integer imageWidth;
        @JsonProperty("image_imgSize")
        private Double imgSize;
    }

}
