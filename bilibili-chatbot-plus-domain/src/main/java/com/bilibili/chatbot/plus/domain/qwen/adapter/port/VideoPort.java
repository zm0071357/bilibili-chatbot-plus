package com.bilibili.chatbot.plus.domain.qwen.adapter.port;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.AnalysisVideoEntity;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface VideoPort {
    /**
     * 获取图片在线视频链接
     * @param url
     * @return
     */
    @POST("/api/sp_jx/sp.php")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    Call<AnalysisVideoEntity> analysis(@Query("url") String url);
}
