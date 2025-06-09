package com.bilibili.chatbot.plus.config;

import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliImagePort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliVideoExtraPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliVideoPort;
import com.bilibili.chatbot.plus.infrastructure.adapter.repository.BilibiliRepositoryImpl;
import com.bilibili.chatbot.plus.infrastructure.adapter.repository.QwenRepositoryImpl;
import com.bilibili.chatbot.plus.trigger.job.BilibiliChatJob;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;


@Slf4j
@Configuration
@EnableConfigurationProperties(BilibiliConfigProperties.class)
public class BilibiliConfig {

    private final BilibiliConfigProperties properties;

    public BilibiliConfig(BilibiliConfigProperties properties) {
        this.properties = properties;
    }

    @Bean("bilibiliRepositoryImpl")
    public BilibiliRepositoryImpl bilibiliRepository(OkHttpClient okHttpClient) {
        BilibiliPort bilibiliPort = new Retrofit.Builder()
                .baseUrl(properties.getUrl())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(BilibiliPort.class);

        BilibiliImagePort bilibiliImagePort = new Retrofit.Builder()
                .baseUrl(properties.getSendImageUrl())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(BilibiliImagePort.class);

        BilibiliVideoPort bilibiliVideoPort = new Retrofit.Builder()
                .baseUrl(properties.getSendVideoUrl())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(BilibiliVideoPort.class);

        BilibiliVideoExtraPort bilibiliVideoExtraPort = new Retrofit.Builder()
                .baseUrl("https://upos-cs-upcdnbldsa.bilivideo.com/") // 假URL
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(BilibiliVideoExtraPort.class);
        log.info("b站AI助手 - b站相关服务配置完成");
        return new BilibiliRepositoryImpl(
                bilibiliPort,
                bilibiliImagePort,
                bilibiliVideoPort,
                bilibiliVideoExtraPort,
                properties.getLoginId(),
                properties.getCookie(),
                properties.getCsrf(),
                properties.getSessionType(),
                properties.getSize(),
                properties.getMobiApp(),
                properties.getReceiverType(),
                okHttpClient
                );
    }

    @Bean("bilibiliChatJob")
    public BilibiliChatJob bilibiliChatJob(QwenRepositoryImpl qwenRepositoryImpl) {
        return new BilibiliChatJob(qwenRepositoryImpl);
    }

}
