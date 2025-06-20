package com.bilibili.chatbot.plus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class ThreadPoolConfig {
    @Bean("bilibili_chatbot_plus_threadPoolExecutor01")
    public ThreadPoolExecutor threadPoolExecutor01(ThreadPoolConfigProperties properties) {
        // 实例化策略
        RejectedExecutionHandler handler;
        switch (properties.getPolicy()){
            case "AbortPolicy":
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }
        log.info("线程池配置完成");
        // 创建线程池
        return new ThreadPoolExecutor(properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler);
    }

//    @Bean("bilibili_chatbot_plus_threadPoolExecutor02")
//    public ThreadPoolExecutor threadPoolExecutor02(ThreadPoolConfigProperties properties) {
//        // 实例化策略
//        RejectedExecutionHandler handler;
//        switch (properties.getPolicy()){
//            case "AbortPolicy":
//                handler = new ThreadPoolExecutor.AbortPolicy();
//                break;
//            case "DiscardPolicy":
//                handler = new ThreadPoolExecutor.DiscardPolicy();
//                break;
//            case "DiscardOldestPolicy":
//                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
//                break;
//            case "CallerRunsPolicy":
//                handler = new ThreadPoolExecutor.CallerRunsPolicy();
//                break;
//            default:
//                handler = new ThreadPoolExecutor.AbortPolicy();
//                break;
//        }
//
//        // 创建线程池
//        return new ThreadPoolExecutor(properties.getCorePoolSize(),
//                properties.getMaxPoolSize(),
//                properties.getKeepAliveTime(),
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(properties.getBlockQueueSize()),
//                Executors.defaultThreadFactory(),
//                handler);
//    }

}
