package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 结束上传视频文件结果实体
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndUploadVideoEntity {
    @JsonProperty("OK")
    private Integer OK;
    private String bucket;
    private String key;
    private String location;

    /**
     * 获取文件名
     * @param key
     * @return
     */
    public String getFileName(String key) {
        return key.replace("/", "")
                  .replace(".zip", "")
                  .replace(".mp4", "");
    }

}
