package com.bilibili.chatbot.plus.domain.bilibili.serivce;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.BilibiliService;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.*;
import com.bilibili.chatbot.plus.domain.qwen.model.QwenResponseEntity;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
public class BilibiliServiceImpl implements BilibiliService {

    @Resource
    private BilibiliRepository bilibiliRepository;
    private final BilibiliPort bilibiliPort;
    private final long loginId;
    private final String cookie;
    private final String csrf;
    private final Integer sessionType;
    private final Integer size;
    private final String mobiApp;
    private final Integer receiverType;

    public BilibiliServiceImpl(BilibiliPort bilibiliPort, long loginId, String cookie, String csrf, Integer sessionType, Integer size, String mobiApp, Integer receiverType) {
        this.bilibiliPort = bilibiliPort;
        this.loginId = loginId;
        this.cookie = cookie;
        this.csrf = csrf;
        this.sessionType = sessionType;
        this.size = size;
        this.mobiApp = mobiApp;
        this.receiverType = receiverType;
    }

    @Override
    public SessionsEntity getSessions() throws IOException {
        Call<SessionsEntity> call = bilibiliPort.getSessions(cookie, sessionType, size, mobiApp);
        Response<SessionsEntity> response = call.execute();
        return response.body();
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return bilibiliRepository.getUnHandleSessionLists(sessionLists);
    }

    @Override
    public void handle(List<SessionsEntity.Data.SessionList> unHandleSessionLists) throws IOException {
        for (SessionsEntity.Data.SessionList unHandleSessionList : unHandleSessionLists) {
            // 参数
            long senderUid = unHandleSessionList.getLastMsg().getSender_uid();
            String contentJSON = unHandleSessionList.getLastMsg().getContent();
            String question = null;
            if (contentJSON.contains(ContentTypeEnum.TEXT.getType())) {
                TextContent textContent = JSON.parseObject(contentJSON, TextContent.class);
                question = textContent.getContent();
            } else if (contentJSON.contains(ContentTypeEnum.IMAGE.getType())) {
                ImageContent imageContent = JSON.parseObject(contentJSON, ImageContent.class);
                question = imageContent.getUrl();
            }
            int msgType = unHandleSessionList.getLastMsg().getMsg_type();
            // 文字消息
            if (MessageTypeEnum.TEXT.getType().equals(msgType)) {
                log.info("获取到用户的文字消息:{},{}", senderUid, question);
                // 预处理
                SendMessageResponseEntity PreResponse = sendMessage(senderUid, MessageTypeEnum.TEXT.getType(), MessageConstant.PRE_MESSAGE);
                log.info("给用户发送预处理消息:{}, code:{}", senderUid, PreResponse.getCode());
                // 调用大模型
                QwenResponseEntity res = bilibiliRepository.handle(senderUid, question, ContentTypeEnum.TEXT.getSign());
                log.info("大模型处理结果:{},{}", senderUid, res.getResult());
                // 结果写回
                SendMessageResponseEntity response = sendMessage(senderUid, MessageTypeEnum.TEXT.getType(), res.getResult());
                log.info("给用户发送处理消息:{}, code:{}", senderUid, response.getCode());
            }
            // 图片消息/自定义表情消息
            else if (MessageTypeEnum.IMAGE.getType().equals(msgType) || MessageTypeEnum.CUSTOM_EMOJI.getType().equals(msgType)) {
                log.info("获取到用户的图片消息:{},{}", senderUid, question);
                // 预处理
                SendMessageResponseEntity PreResponse = sendMessage(senderUid, MessageTypeEnum.TEXT.getType(), MessageConstant.PRE_MESSAGE);
                log.info("给用户发送预处理消息:{}, code:{}", senderUid, PreResponse.getCode());
                // 调用大模型
                QwenResponseEntity res = bilibiliRepository.handle(senderUid, question, ContentTypeEnum.IMAGE.getSign());
                log.info("大模型处理结果:{},{}", senderUid, res.getResult());
                // 结果写回
                SendMessageResponseEntity response = sendMessage(senderUid, MessageTypeEnum.TEXT.getType(), res.getResult());
                log.info("给用户发送处理消息:{}, code:{}", senderUid, response.getCode());
            }
        }
    }

    @Override
    public SendMessageResponseEntity sendMessage(long receiverId, Integer msgType, String content) throws IOException {
        String devId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        System.out.println(MessageConstant.getContent(content));
        Call<SendMessageResponseEntity> call = bilibiliPort.sendMessage(cookie, loginId, receiverId, receiverType, msgType, devId, timestamp, MessageConstant.getContent(content), csrf, csrf);
        Response<SendMessageResponseEntity> response = call.execute();
        return response.body();
    }
}
