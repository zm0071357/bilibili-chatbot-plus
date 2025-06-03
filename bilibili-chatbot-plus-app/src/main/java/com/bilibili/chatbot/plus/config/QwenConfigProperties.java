package com.bilibili.chatbot.plus.config;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "qwen.sdk.config", ignoreInvalidFields = true)
public class QwenConfigProperties {
    private boolean enable;
    private String apiKey;
    private String analysisVideoUrl;
}
