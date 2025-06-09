package com.bilibili.chatbot.plus.domain.bilibili.adapter.port;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.*;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface BilibiliVideoPort {

    /**
     * 上传视频封面
     * @param cookie
     * @param csrf
     * @param cover
     * @param ts
     * @return
     */
    @POST("/x/vu/web/cover/up")
    @FormUrlEncoded
    Call<SendCoverResponseEntity> sendCover(@Header("Cookie") String cookie,
                                            @Field("csrf") String csrf,
                                            @Field("cover") String cover,
                                            @Query("ts") long ts);

    /**
     * 获取上传元数据 (预上传)
     * @param cookie
     * @param name
     * @param r
     * @param profile
     * @return
     */
    @GET("/preupload")
    Call<PreUploadResponseEntity> preUpload(@Header("Cookie") String cookie,
                                            @Query("name") String name,
                                            @Query("r") String r,
                                            @Query("profile") String profile);

    /**
     * 投递视频稿件
     * @return
     */
    @POST("/x/vu/web/add/v3")
    @Headers("Content-Type: application/json")
    Call<SendVideoResponseEntity> sendVideo(@Header("Cookie") String cookie,
                                            @Query("t") long t,
                                            @Query("csrf") String csrf,
                                            @Body RequestBody requestBody);

}
