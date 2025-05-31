package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisVideoEntity {
    private Data data;
    private int code;
    private String msg;
    private Text text;

    @Getter
    public static class Data {
        private String author;
        private String avatar;
        private String time;
        private String like;
        private String title;
        private String cover;
        private String url;
    }

    @Getter
    public static class Text {
        private String msg;
        private String time;
    }

}
