package com.bilibili.chatbot.plus.domain.bilibili.adapter.port;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import retrofit2.Call;
import retrofit2.http.*;

import java.io.IOException;

public interface BilibiliPort {

    /**
     * 获取会话
     * @param cookie
     * @param sessionType
     * @param size
     * @param mobiApp
     * @return
     * @throws IOException
     */
    @GET("/session_svr/v1/session_svr/get_sessions")
    @Headers("TextContent-Type: application/json; charset=utf-8")
    Call<SessionsEntity> getSessions(@Header("Cookie") String cookie,
                                     @Query("session_type") Integer sessionType,
                                     @Query("size") Integer size,
                                     @Query("mobi_app") String mobiApp) throws IOException;

    /**
     * 发送消息
     * @param cookie
     * @param sendUid
     * @param receiverId
     * @param receiverType
     * @param msgType
     * @param devId
     * @param timestamp
     * @param content
     * @param csrf
     * @param csrfToken
     * @return
     */
    @POST("/web_im/v1/web_im/send_msg")
    @Headers("TextContent-Type: application/x-www-form-urlencoded")
    Call<SendMessageResponseEntity> sendMessage(@Header("Cookie") String cookie,
                                                @Query("msg[sender_uid]") long sendUid,
                                                @Query("msg[receiver_id]") long receiverId,
                                                @Query("msg[receiver_type]") Integer receiverType,
                                                @Query("msg[msg_type]") Integer msgType,
                                                @Query("msg[dev_id]") String devId,
                                                @Query("msg[timestamp]") long timestamp,
                                                @Query("msg[content]") String content,
                                                @Query("csrf") String csrf,
                                                @Query("csrf_token") String csrfToken);
}
