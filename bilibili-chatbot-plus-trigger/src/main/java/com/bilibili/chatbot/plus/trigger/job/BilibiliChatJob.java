package com.bilibili.chatbot.plus.trigger.job;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.BilibiliService;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class BilibiliChatJob {

    @Resource
    private BilibiliService bilibiliService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void exec() {
        try {
            log.info("定时任务开始，获取对话列表");
            // 获取对话列表
            SessionsEntity sessionEntity = bilibiliService.getSessions();
            log.info("sessionEntity:{}", JSON.toJSONString(sessionEntity));
        } catch (Exception e) {
            log.info("定时任务出错:{}", e.getMessage());
        }
    }
}
