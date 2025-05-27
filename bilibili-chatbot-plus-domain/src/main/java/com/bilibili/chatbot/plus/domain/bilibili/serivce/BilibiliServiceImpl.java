package com.bilibili.chatbot.plus.domain.bilibili.serivce;

import com.bilibili.chatbot.plus.domain.bilibili.BilibiliService;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
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

    private final String cookie;

    private final Integer sessionType;

    private final Integer size;

    private final String mobiApp;

    public BilibiliServiceImpl(String cookie, Integer sessionType, Integer size, String mobiApp) {
        this.cookie = cookie;
        this.sessionType = sessionType;
        this.size = size;
        this.mobiApp = mobiApp;
    }

    @Override
    public SessionsEntity getSessions() throws IOException {
        Call<SessionsEntity> call = bilibiliRepository.getSessions(cookie, sessionType, size, mobiApp);
        Response<SessionsEntity> response = call.execute();
        return response.body();
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return null;
    }

    @Override
    public void handle(List<SessionsEntity.Data.SessionList> unHandleSessionLists) {

    }
}
