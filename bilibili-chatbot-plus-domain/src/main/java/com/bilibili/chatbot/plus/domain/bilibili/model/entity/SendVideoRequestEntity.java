package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import lombok.*;

import java.util.List;

/**
 * 投递视频稿件请求实体
 */
@Getter
@Builder
public class SendVideoRequestEntity {
    private List<VideoEntity.Video> videos;
    private String cover;
    private String cover43;
    private String title;
    private Integer copyright;
    private Integer tid;
    private String tag;
    private Integer desc_format_id;
    private String desc;
    private Integer recreate;
    private String dynamic;
    private Integer interactive;
    private Integer act_reserve_create;
    private Integer ai_cover;
    private Integer no_disturbance;
    private Integer no_reprint;
    private SubTitleEntity subtitle;
    private Integer dobly;
    private Integer lossless_music;
    private boolean up_selection_reply;
    private boolean up_close_reply;
    private boolean up_close_danmu;
    private Integer web_os;

}
