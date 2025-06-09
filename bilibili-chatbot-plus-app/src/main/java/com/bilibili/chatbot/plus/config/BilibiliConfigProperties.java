package com.bilibili.chatbot.plus.config;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "bilibili.chatbot.plus.config", ignoreInvalidFields = true)
public class BilibiliConfigProperties {

    private String url;
    private String sendImageUrl;
    private String sendVideoUrl;
    private long loginId;
    private String cookie;
    private String csrf;
    private Integer sessionType;
    private Integer size;
    private String mobiApp;
    private Integer receiverType = 1;

}
