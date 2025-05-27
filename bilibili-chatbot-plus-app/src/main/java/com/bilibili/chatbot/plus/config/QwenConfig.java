package com.bilibili.chatbot.plus.config;

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

@Configuration
@EnableConfigurationProperties(QwenConfigProperties.class)
public class QwenConfig {

    private final Logger logger = LoggerFactory.getLogger(QwenConfig.class);

    @Bean(name = "modelFactory")
    public ModelFactory modelFactory(QwenConfigProperties properties) {
        qwen.sdk.factory.Configuration configuration = new qwen.sdk.factory.Configuration(properties.getApiKey());
        logger.info("通义千问配置完成");
        return new DefaultModelFactory(configuration);
    }

    @Bean(name = "chatService")
    @ConditionalOnProperty(value = "qwen.sdk.config.enable", havingValue = "true", matchIfMissing = false)
    public ChatServiceImpl chatService(ModelFactory modelFactory) {
        logger.info("对话服务装配完成");
        return modelFactory.chatService();
    }

    @Bean(name = "imageService")
    @ConditionalOnProperty(value = "qwen.sdk.config.enable", havingValue = "true", matchIfMissing = false)
    public ImageServiceImpl imageService(ModelFactory modelFactory) {
        logger.info("生成图像服务装配完成");
        return modelFactory.imageService();
    }

    @Bean(name = "videoService")
    @ConditionalOnProperty(value = "qwen.sdk.config.enable", havingValue = "true", matchIfMissing = false)
    public VideoServiceImpl videoService(ModelFactory modelFactory) {
        logger.info("生成视频服务装配完成");
        return modelFactory.videoService();
    }
}
