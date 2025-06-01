package com.bilibili.chatbot.plus.domain.bilibili.adapter.port;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendImageResponseEntity;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.io.IOException;

public interface BilibiliImagePort {

    @POST("/x/dynamic/feed/draw/upload_bfs")
    //@Headers("Content-Type: application/json; charset=utf-8")
    @Multipart
    Call<SendImageResponseEntity> sendImage(@Header("Cookie") String cookie,
                                            @Part MultipartBody.Part url,
                                            @Query("biz") String biz,
                                            @Query("csrf") String csrf) throws IOException;
}
