package com.bilibili.chatbot.plus.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties(OkHttpClientProperties.class)
public class OkHttpClientConfig {

    @Bean
    public OkHttpClient httpClient(OkHttpClientProperties properties) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(properties.getLevel());
        // 开启HTTP客户端
        return new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(properties.getConnectTimeOut(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeOut(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeOut(), TimeUnit.SECONDS)
                .build();
    }
}
