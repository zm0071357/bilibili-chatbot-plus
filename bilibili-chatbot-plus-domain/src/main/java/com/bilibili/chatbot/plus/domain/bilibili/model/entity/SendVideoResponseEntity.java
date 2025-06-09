package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import lombok.*;

/**
 * 投递视频稿件结果实体
 */
@Getter
public class SendVideoResponseEntity {
    private Integer code;
    private String message;
    private Integer ttl;
    private Data data;

    @Getter
    public static class Data {
        private String aid;
        private String bvid;
        private String v_voucher;
    }
}
