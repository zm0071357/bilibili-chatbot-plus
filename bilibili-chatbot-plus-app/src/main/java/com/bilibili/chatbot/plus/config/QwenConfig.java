package com.bilibili.chatbot.plus.config;

import com.bilibili.chatbot.plus.domain.qwen.adapter.port.VideoPort;
import com.bilibili.chatbot.plus.infrastructure.adapter.repository.QwenRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import qwen.sdk.factory.ModelFactory;
import qwen.sdk.factory.defaults.DefaultModelFactory;
import qwen.sdk.largemodel.chat.impl.ChatServiceImpl;
import qwen.sdk.largemodel.image.impl.ImageServiceImpl;
import qwen.sdk.largemodel.video.impl.VideoServiceImpl;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties(QwenConfigProperties.class)
public class QwenConfig {

    private final QwenConfigProperties properties;

    public QwenConfig(QwenConfigProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "modelFactory")
    public ModelFactory modelFactory(QwenConfigProperties properties) {
        qwen.sdk.factory.Configuration configuration = new qwen.sdk.factory.Configuration(properties.getApiKey());
        log.info("通义千问配置完成");
        return new DefaultModelFactory(configuration);
    }

    @Bean(name = "chatService")
    @ConditionalOnProperty(value = "qwen.sdk.config.enable", havingValue = "true", matchIfMissing = false)
    public ChatServiceImpl chatService(ModelFactory modelFactory) {
        log.info("对话服务装配完成");
        return modelFactory.chatService();
    }

    @Bean(name = "imageService")
    @ConditionalOnProperty(value = "qwen.sdk.config.enable", havingValue = "true", matchIfMissing = false)
    public ImageServiceImpl imageService(ModelFactory modelFactory) {
        log.info("生成图像服务装配完成");
        return modelFactory.imageService();
    }

    @Bean(name = "videoService")
    @ConditionalOnProperty(value = "qwen.sdk.config.enable", havingValue = "true", matchIfMissing = false)
    public VideoServiceImpl videoService(ModelFactory modelFactory) {
        log.info("生成视频服务装配完成");
        return modelFactory.videoService();
    }

    @Bean(name = "qwenRepositoryImpl")
    public QwenRepositoryImpl qwenRepositoryImpl(OkHttpClient okHttpClient, ChatServiceImpl chatServiceImpl, ImageServiceImpl imageServiceImpl, VideoServiceImpl videoServiceImpl) {
        VideoPort videoPort = new Retrofit.Builder()
                .baseUrl(properties.getAnalysisVideoUrl())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(VideoPort.class);
        log.info("b站AI助手 - 通义千问大模型相关服务配置完成");
        return new QwenRepositoryImpl(videoPort, chatServiceImpl, imageServiceImpl, videoServiceImpl);
    }
}
