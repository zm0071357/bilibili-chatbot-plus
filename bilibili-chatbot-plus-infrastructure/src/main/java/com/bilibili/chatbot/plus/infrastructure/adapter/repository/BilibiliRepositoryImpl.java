package com.bilibili.chatbot.plus.infrastructure.adapter.repository;

import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.ReceiverTypeEnum;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BilibiliRepositoryImpl implements BilibiliRepository{

    private final BilibiliRepository bilibiliRepository;

    private final long loginId;

    public BilibiliRepositoryImpl(BilibiliRepository bilibiliRepository, long loginId) {
        this.bilibiliRepository = bilibiliRepository;
        this.loginId = loginId;
    }

    @Override
    public Call<SessionsEntity> getSessions(String cookie, Integer sessionType, Integer size, String mobiApp) throws IOException {
        return bilibiliRepository.getSessions(cookie, sessionType, size, mobiApp);
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return sessionLists.stream()
                .filter(session -> session.getLastMsg() != null && session.getLastMsg().getSender_uid() != loginId)
                .collect(Collectors.toList());
    }

    @Override
    public void handle(List<SessionsEntity.Data.SessionList> unHandleSessionLists) {

    }

    @Override
    public Call<SendMessageResponseEntity> sendMessage(String cookie, long sendUid, long receiverId, Integer receiverType, Integer msgType, String devId, long timestamp, String content, String csrf, String csrfToken) {
        return bilibiliRepository.sendMessage(cookie, sendUid, receiverId, ReceiverTypeEnum.USER.getType(), msgType, devId, timestamp, content, csrf, csrfToken);
    }

}
