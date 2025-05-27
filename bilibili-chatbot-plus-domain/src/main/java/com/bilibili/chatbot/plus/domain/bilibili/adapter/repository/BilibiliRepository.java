package com.bilibili.chatbot.plus.domain.bilibili.adapter.repository;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.io.IOException;
import java.util.List;

public interface BilibiliRepository {

    @GET("/session_svr/v1/session_svr/get_sessions")
    @Headers("Content-Type: application/json; charset=utf-8")
    Call<SessionsEntity> getSessions(@Header("Cookie") String cookie,
                                     @Query("session_type") Integer sessionType,
                                     @Query("size") Integer size,
                                     @Query("mobi_app") String mobiApp) throws IOException;

    List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists);

    void handle(List<SessionsEntity.Data.SessionList> unHandleSessionLists);
}
