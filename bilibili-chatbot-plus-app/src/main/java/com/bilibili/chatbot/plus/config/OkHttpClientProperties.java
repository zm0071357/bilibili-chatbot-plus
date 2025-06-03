package com.bilibili.chatbot.plus.config;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "okhttpclient.config", ignoreInvalidFields = true)
public class OkHttpClientProperties {
    private HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.HEADERS;
    private long connectTimeOut;
    private long writeTimeOut;
    private long readTimeOut;
}
