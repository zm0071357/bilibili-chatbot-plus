package com.bilibili.chatbot.plus.trigger.job;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.BilibiliService;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
            List<SessionsEntity.Data.SessionList> sessionLists = sessionEntity.getData().getSessionList();
            // 获取未回复的对话
            List<SessionsEntity.Data.SessionList> unHandleSessionLists = bilibiliService.getUnHandleSessionLists(sessionLists);
            int size = unHandleSessionLists.size();
            log.info("本次获取到 {} 条未回复对话", size);
            // 处理
            if (size > 0) {
                log.info("开始处理未回复对话");
                bilibiliService.handle(unHandleSessionLists);
            }
        } catch (Exception e) {
            log.info("定时任务出错:{}", e.getMessage());
        }
    }
}
