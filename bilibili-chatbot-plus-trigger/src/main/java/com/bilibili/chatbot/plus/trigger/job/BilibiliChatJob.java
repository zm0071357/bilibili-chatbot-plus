package com.bilibili.chatbot.plus.trigger.job;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.BilibiliService;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.MessageContextEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SubmitVideoResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.*;
import com.bilibili.chatbot.plus.domain.qwen.QwenService;
import com.bilibili.chatbot.plus.domain.qwen.model.entity.QwenResponseEntity;
import com.bilibili.chatbot.plus.infrastructure.adapter.repository.QwenRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import qwen.sdk.largemodel.chat.model.ChatRequest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class BilibiliChatJob {

    @Resource
    private BilibiliService bilibiliService;

    @Resource
    private QwenService qwenService;

    private final QwenRepositoryImpl qwenRepositoryImpl;

    private final ThreadPoolExecutor threadPoolExecutor;

    public BilibiliChatJob(QwenRepositoryImpl qwenRepositoryImpl, ThreadPoolExecutor threadPoolExecutor) {
        this.qwenRepositoryImpl = qwenRepositoryImpl;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * 核心任务 - 处理对话
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void exec() {
        try {
            log.info("定时任务开始：获取对话列表");
            // 获取对话列表
            SessionsEntity sessionEntity = bilibiliService.getSessions();
            List<SessionsEntity.Data.SessionList> sessionLists = sessionEntity.getData().getSessionList();
            // 获取未回复的对话
            List<SessionsEntity.Data.SessionList> unHandleSessionLists = bilibiliService.getUnHandleSessionLists(sessionLists);
            int size = unHandleSessionLists.size();
            log.info("本次获取到 {} 条未回复对话", size);
            // 处理
            if (size > 0) {
                log.info("开始处理未回复对话");
                for (SessionsEntity.Data.SessionList unHandleSessionList : unHandleSessionLists) {
                    // 使用线程池同时处理多条对话
                    threadPoolExecutor.execute(() -> {
                        try {
                            handleSingleSessionList(unHandleSessionList);
                        } catch (Exception e) {
                            log.error("处理对话时出错: {}", e.getMessage(), e);
                        }
                    });
                }
            }
            log.info("定时任务完成");
        } catch (Exception e) {
            log.info("定时任务出错:{}", e.getMessage());
        }
    }

    /**
     * 核心任务 - 控制历史记录
     * 限制 token 防止请求失败
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void history() {
        log.info("定时任务开始：获取历史记录集合");
        Map<Long, List<ChatRequest.Input.Message>> history = qwenRepositoryImpl.getHistory();
        for (Map.Entry<Long, List<ChatRequest.Input.Message>> entry : history.entrySet()) {
            Long userId = entry.getKey();
            List<ChatRequest.Input.Message> messages = entry.getValue();
            int size = messages.size();
            if (size > 20) {
                log.info("用户 {} 的历史记录超过20条（当前: {} 条），保留最近20条", userId, size);
                List<ChatRequest.Input.Message> retainedMessages = new ArrayList<>();
                retainedMessages.add(messages.get(0));
                int startIndex = size - 19;
                retainedMessages.addAll(messages.subList(startIndex, size));
                history.put(userId, retainedMessages);
                log.info("用户 {} 历史记录清理完成，保留 {} 条记录（系统消息1条 + 最近交互消息{}条）",
                        userId, retainedMessages.size(), retainedMessages.size() - 1);
            }
        }
        log.info("定时任务完成");
    }

    /**
     * 文本处理
     * @param text 文本
     * @return 文本集合
     */
    private List<String> handleText(String text) {
        List<String> textPieceList = new ArrayList<>();
        for (int start = 0; start < text.length(); start += 500) {
            int end = Math.min(start + 500, text.length());
            textPieceList.add(text.substring(start, end));
        }
        return textPieceList;
    }

    /**
     * 处理单个对话
     * @param unHandleSessionList 对话
     * @throws IOException
     */
    private void handleSingleSessionList(SessionsEntity.Data.SessionList unHandleSessionList) throws IOException {
        // 参数
        long senderUid = unHandleSessionList.getLastMsg().getSender_uid();
        int msgType = unHandleSessionList.getLastMsg().getMsg_type();
        String contentJSON = unHandleSessionList.getLastMsg().getContent();
        String question = null;
        ShareContent shareContent = null;

        // 发送预处理通知
        SendMessageResponseEntity sendPreMessageResponse = bilibiliService.sendTextMessage(senderUid, MessageTypeEnum.TEXT.getType(), MessageConstant.PRE_MESSAGE);
        log.info("给用户发送预处理消息:{}, code:{}", senderUid, sendPreMessageResponse.getCode());

        // 转换消息实体
        if (contentJSON.contains(ContentTypeEnum.TEXT.getType())) {
            TextContent textContent = JSON.parseObject(contentJSON, TextContent.class);
            question = textContent.getContent();
        } else if (contentJSON.contains(ContentTypeEnum.IMAGE.getType())) {
            ImageContent imageContent = JSON.parseObject(contentJSON, ImageContent.class);
            question = imageContent.getUrl();
        } else if (contentJSON.contains(ContentTypeEnum.SHARE.getType())) {
            shareContent = JSON.parseObject(contentJSON, ShareContent.class);
        }

        // 大模型处理
        QwenResponseEntity response = qwenService.handle(MessageContextEntity.builder()
                .senderUid(senderUid)
                .msgType(msgType)
                .question(question)
                .shareContent(shareContent)
                .build());

        // 发送处理结果
        // 文字消息
        if (response.isText()) {
            String text = String.valueOf(response.getResult());
            if (text.length() < 500) {
                SendMessageResponseEntity sendMessageResponse = bilibiliService.sendTextMessage(senderUid, MessageTypeEnum.TEXT.getType(), text);
                log.info("给用户发送处理消息:{}, code:{}", senderUid, sendMessageResponse.getCode());
            } else {
                List<String> textList = handleText(text);
                for (String textPiece : textList) {
                    SendMessageResponseEntity sendMessageResponse = bilibiliService.sendTextMessage(senderUid, MessageTypeEnum.TEXT.getType(), textPiece);
                    log.info("给用户发送处理消息:{}, code:{}", senderUid, sendMessageResponse.getCode());
                }
            }
        } else if (response.isImage()){
            // 图片消息
            String url = String.valueOf(response.getResult());
            SendMessageResponseEntity sendImageMessageResponse = bilibiliService.sendImageMessage(senderUid, MessageTypeEnum.IMAGE.getType(), url);
            log.info("给用户发送处理消息:{}, code:{}", senderUid, sendImageMessageResponse.getCode());
        } else if (response.isVideo()) {
            // 视频消息
            String url = String.valueOf(response.getResult());
            // 将视频上传至b站
            SubmitVideoResponseEntity submitVideoResponse = bilibiliService.uploadVideo(url);
            String text;
            SendMessageResponseEntity sendImageMessageResponse;
            if (submitVideoResponse.isSuccess()) {
                text = "视频生成成功，下载地址：\n" + "\n" + url + "\n" + "\n请复制这段链接后打开任意一个浏览器下载，另外视频已成功投稿至b站，稍等审核通过后即可通过BV号查看生成的视频哦~\n" + "BV号为：" + submitVideoResponse.getResult();
                sendImageMessageResponse = bilibiliService.sendTextMessage(senderUid, MessageTypeEnum.TEXT.getType(), text);
            } else {
                text = "视频生成成功，下载地址：\n" + url + submitVideoResponse.getResult();
                sendImageMessageResponse = bilibiliService.sendTextMessage(senderUid, MessageTypeEnum.TEXT.getType(), text);
            }
            log.info("给用户发送处理消息:{}, code:{}", senderUid, sendImageMessageResponse.getCode());
        }
    }
}
