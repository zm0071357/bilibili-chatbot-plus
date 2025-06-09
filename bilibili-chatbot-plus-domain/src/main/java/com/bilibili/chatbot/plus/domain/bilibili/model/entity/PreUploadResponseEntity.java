package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * 获取上传元数据结果实体
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreUploadResponseEntity {
    @JsonProperty("OK")
    private Integer OK;
    private String auth;
    private long biz_id;
    private Integer chunk_retry;
    private Integer chunk_retry_delay;
    private Integer chunk_size;
    private String endpoint;
    private List<String> endpoints;
    private Integer threads;
    private Integer timeout;
    private String uip;
    private String upos_uri;

    /**
     * 获取上传元数据URL
     * @param endpoint
     * @param upos_uri
     * @return
     */
    public String getUploadMetadataUrl(String endpoint, String upos_uri) {
        String handleUposUri = upos_uri.replace("upos://", "");
        return "https:" + endpoint + "/" + handleUposUri + "?uploads";
    }

    /**
     * 获取分片上传视频文件URL
     * @param endpoint
     * @param upos_uri
     * @return
     */
    public String getFragmentUploadUrl(String endpoint, String upos_uri) {
        String handleUposUri = upos_uri.replace("upos://", "");
        return "https:" + endpoint + "/" + handleUposUri;
    }

}
