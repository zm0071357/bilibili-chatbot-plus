package com.bilibili.chatbot.plus.infrastructure.adapter.repository;

import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import org.springframework.stereotype.Repository;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;

@Repository
public class BilibiliRepositoryImpl implements BilibiliRepository{

    private final BilibiliRepository bilibiliRepository;

    public BilibiliRepositoryImpl(BilibiliRepository bilibiliRepository) {
        this.bilibiliRepository = bilibiliRepository;
    }

    @Override
    public Call<SessionsEntity> getSessions(String cookie, Integer sessionType, Integer size, String mobiApp) throws IOException {
        return bilibiliRepository.getSessions(cookie, sessionType, size, mobiApp);
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return null;
    }

    @Override
    public void handle(List<SessionsEntity.Data.SessionList> unHandleSessionLists) {

    }
}
