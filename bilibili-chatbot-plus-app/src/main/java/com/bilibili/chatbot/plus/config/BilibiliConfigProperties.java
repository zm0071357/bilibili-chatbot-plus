package com.bilibili.chatbot.plus.config;

import lombok.Data;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "bilibili.chatbot.plus.config", ignoreInvalidFields = true)
public class BilibiliConfigProperties {
    private String url;
    private long loginId;
    private String cookie;
    private String csrf;
    private Integer sessionType;
    private Integer size;
    private String mobiApp;
    @Setter
    private OkHttpClient okHttpClient;
    @Setter
    private HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.HEADERS;
    @Setter
    private long connectTimeOut = 60;
    @Setter
    private long writeTimeOut = 60;
    @Setter
    private long readTimeOut = 60;

}
