package com.bilibili.chatbot.plus.domain.bilibili.adapter.port;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendImageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.io.IOException;

public interface BilibiliImagePort {

    @GET("/dynamic/feed/draw/upload_bfs")
    @Headers("TextContent-Type: application/x-www-form-urlencoded")
    Call<SendImageResponseEntity> sendImage(@Header("Cookie") String cookie,
                                            @Query("") String imageUrl,
                                            @Query("") Integer size,
                                            @Query("") String mobiApp) throws IOException;
}
