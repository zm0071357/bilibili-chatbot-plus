package com.bilibili.chatbot.plus.config;

import com.bilibili.chatbot.plus.infrastructure.adapter.repository.BilibiliRepositoryImpl;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.serivce.BilibiliServiceImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties(BilibiliConfigProperties.class)
public class BilibiliConfig {

    private final Logger logger = LoggerFactory.getLogger(BilibiliConfig.class);

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
        return new BilibiliServiceImpl(properties.getLoginId(), properties.getCookie(), properties.getCsrf(), properties.getSessionType(), properties.getSize(), properties.getMobiApp());
    }

    @Bean("bilibiliRepository")
    public BilibiliRepositoryImpl bilibiliRepository() {
        BilibiliRepository bilibiliRepository = new Retrofit.Builder()
                .baseUrl(properties.getUrl())
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(BilibiliRepository.class);
        return new BilibiliRepositoryImpl(bilibiliRepository, properties.getLoginId());
    }
}
