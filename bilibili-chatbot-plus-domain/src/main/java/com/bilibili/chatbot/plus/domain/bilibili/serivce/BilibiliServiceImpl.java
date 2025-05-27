package com.bilibili.chatbot.plus.domain.bilibili.serivce;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.BilibiliService;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageTypeEnum;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.ReceiverTypeEnum;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant.*;

@Slf4j
public class BilibiliServiceImpl implements BilibiliService {

    @Resource
    private BilibiliRepository bilibiliRepository;

    private final long loginId;

    private final String cookie;

    private final String csrf;

    private final Integer sessionType;

    private final Integer size;

    private final String mobiApp;

    public BilibiliServiceImpl(long loginId, String cookie, String csrf, Integer sessionType, Integer size, String mobiApp) {
        this.loginId = loginId;
        this.cookie = cookie;
        this.csrf = csrf;
        this.sessionType = sessionType;
        this.size = size;
        this.mobiApp = mobiApp;
    }

    @Override
    public SessionsEntity getSessions() throws IOException {
        Call<SessionsEntity> call = bilibiliRepository.getSessions(cookie, sessionType, size, mobiApp);
        Response<SessionsEntity> response = call.execute();
        log.info(JSON.toJSONString(response));
        return response.body();
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return bilibiliRepository.getUnHandleSessionLists(sessionLists);
    }

    @Override
    public void handle(List<SessionsEntity.Data.SessionList> unHandleSessionLists) throws IOException {
        for (SessionsEntity.Data.SessionList sessionList : unHandleSessionLists) {
            // 参数
            int sessionType = sessionList.getSession_type();
            long userId = sessionList.getLastMsg().getSender_uid();
            // 文字消息
            if (MessageTypeEnum.TEXT.getType().equals(sessionType)) {
                Call<SendMessageResponseEntity> call = bilibiliRepository.sendMessage(cookie, loginId, userId, ReceiverTypeEnum.USER.getType(),
                        MessageTypeEnum.TEXT.getType(), UUID.randomUUID().toString(), System.currentTimeMillis(),
                        MessageConstant.getContent(TEXT_MESSAGE)
                        , csrf, csrf);
                Response<SendMessageResponseEntity> response = call.execute();
                log.info("response:{}", JSON.toJSONString(response.body()));
            }
            // 图片消息 / 自定义表情消息
            else if (MessageTypeEnum.IMAGE.getType().equals(sessionType) || MessageTypeEnum.CUSTOM_EMOJI.getType().equals(sessionType)) {
            }
            // 分享消息 - 视频总结
            else if (MessageTypeEnum.SHARE.getType().equals(sessionType)) {
            }
            // 通知消息
            else if (MessageTypeEnum.NOTIFY.getType().equals(sessionType)) {
            }
            else {
            }
        }
    }
}
