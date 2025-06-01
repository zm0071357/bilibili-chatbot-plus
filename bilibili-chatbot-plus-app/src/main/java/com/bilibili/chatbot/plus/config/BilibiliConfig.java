package com.bilibili.chatbot.plus.config;

import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliImagePort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliVideoPort;
import com.bilibili.chatbot.plus.domain.bilibili.serivce.BilibiliServiceImpl;
import com.bilibili.chatbot.plus.infrastructure.adapter.repository.BilibiliRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import qwen.sdk.largemodel.chat.impl.ChatServiceImpl;
import qwen.sdk.largemodel.image.impl.ImageServiceImpl;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties(BilibiliConfigProperties.class)
public class BilibiliConfig {

    private final OkHttpClient httpClient;

    private final BilibiliConfigProperties properties;

    public BilibiliConfig(BilibiliConfigProperties properties) {
        this.properties = properties;
        // 日志配置
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(properties.getLevel());
        // 开启HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(properties.getConnectTimeOut(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeOut(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeOut(), TimeUnit.SECONDS)
                .build();
        log.info("HTTP客户端配置完成");
    }

    @Bean("bilibiliService")
    public BilibiliServiceImpl bilibiliService() {
        BilibiliPort bilibiliPort = new Retrofit.Builder()
                .baseUrl(properties.getUrl())
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(BilibiliPort.class);

        BilibiliImagePort bilibiliImagePort = new Retrofit.Builder()
                .baseUrl(properties.getSendImageUrl())
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(BilibiliImagePort.class);

        BilibiliVideoPort bilibiliVideoPort = new Retrofit.Builder()
                .baseUrl(properties.getAnalysisVideoUrl())
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(BilibiliVideoPort.class);

        log.info("b站AI助手服务装配完成");
        return new BilibiliServiceImpl(bilibiliPort,
                bilibiliImagePort,
                bilibiliVideoPort,
                properties.getLoginId(),
                properties.getCookie(),
                properties.getCsrf(),
                properties.getSessionType(),
                properties.getSize(),
                properties.getMobiApp(),
                properties.getReceiverType());
    }

    @Bean("bilibiliRepositoryImpl")
    public BilibiliRepositoryImpl bilibiliRepository(ChatServiceImpl chatServiceImpl, ImageServiceImpl imageServiceImpl) {
        return new BilibiliRepositoryImpl(properties.getLoginId(),
                chatServiceImpl,
                imageServiceImpl);
    }

}
