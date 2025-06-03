package com.bilibili.chatbot.plus.config;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "bilibili.chatbot.plus.config", ignoreInvalidFields = true)
public class BilibiliConfigProperties {

    private String url;
    private String sendImageUrl;
    private long loginId;
    private String cookie;
    private String csrf;
    private Integer sessionType;
    private Integer size;
    private String mobiApp;
    private Integer receiverType = 1;
    private HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.HEADERS;
    private long connectTimeOut = 60;
    private long writeTimeOut = 60;
    private long readTimeOut = 60;

}
