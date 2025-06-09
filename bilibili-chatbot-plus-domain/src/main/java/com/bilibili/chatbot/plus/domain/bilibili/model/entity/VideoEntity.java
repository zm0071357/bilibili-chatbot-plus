package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoEntity {
    private List<Video> videos;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Video {
        private String filename;
        private String title;
        private String desc;
        private long cid;
    }
}
