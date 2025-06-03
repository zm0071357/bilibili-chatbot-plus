package com.bilibili.chatbot.plus.domain.bilibili.adapter.port;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendImageResponseEntity;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.io.IOException;

public interface BilibiliImagePort {

    /**
     * 发送图片至b站图床
     * @param cookie
     * @param fileUp
     * @param biz
     * @param csrf
     * @return
     * @throws IOException
     */
    @POST("/x/dynamic/feed/draw/upload_bfs")
    @Multipart
    Call<SendImageResponseEntity> sendImage(@Header("Cookie") String cookie,
                                            @Part MultipartBody.Part fileUp,
                                            @Query("biz") String biz,
                                            @Query("csrf") String csrf) throws IOException;
}
