package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 上传视频元数据结果实体
 */
@Getter
public class UploadMetadataResponseEntity {

    @JsonProperty("OK")
    private Integer OK;
    private String bucket;
    private String key;
    private String upload_id;
}
