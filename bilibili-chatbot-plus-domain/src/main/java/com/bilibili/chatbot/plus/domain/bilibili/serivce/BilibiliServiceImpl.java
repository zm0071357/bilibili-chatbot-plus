package com.bilibili.chatbot.plus.domain.bilibili.serivce;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.BilibiliService;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.Content;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

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
    private final String devId;
    private final long timestamp;

    public BilibiliServiceImpl(BilibiliPort bilibiliPort, long loginId, String cookie, String csrf, Integer sessionType, Integer size, String mobiApp, Integer receiverType, String devId, long timestamp) {
        this.bilibiliPort = bilibiliPort;
        this.loginId = loginId;
        this.cookie = cookie;
        this.csrf = csrf;
        this.sessionType = sessionType;
        this.size = size;
        this.mobiApp = mobiApp;
        this.receiverType = receiverType;
        this.devId = devId;
        this.timestamp = timestamp;
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
            String content = JSON.parseObject(contentJSON, Content.class).getContent();
            int msgType = unHandleSessionList.getLastMsg().getMsg_type();
            if (MessageTypeEnum.TEXT.getType().equals(msgType)) {
                log.info("获取到用户的文字消息:{},{}", senderUid, content);
                // 预处理
                SendMessageResponseEntity response = sendMessage(senderUid, receiverType, MessageConstant.TEXT_MESSAGE);
                log.info("给用户发送预处理消息:{}, code:{}", senderUid, response.getCode());
            }
        }
    }

    @Override
    public SendMessageResponseEntity sendMessage(long receiverId, Integer msgType, String content) throws IOException {
        String newContent = "{\"content\":\"" + content + "\"}";
        Call<SendMessageResponseEntity> call = bilibiliPort.sendMessage(cookie, loginId, receiverId, receiverType, msgType, devId, timestamp, newContent, csrf, csrf);
        Response<SendMessageResponseEntity> response = call.execute();
        return response.body();
    }
}
