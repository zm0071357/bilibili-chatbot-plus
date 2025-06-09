package com.bilibili.chatbot.plus.domain.bilibili.adapter.repository;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SubmitVideoResponseEntity;

import java.io.IOException;
import java.util.List;

public interface BilibiliRepository {

    /**
     * 获取会话
     * @return
     */
    SessionsEntity getSessions() throws IOException;

    /**
     * 获取未处理列表
     * @param sessionLists
     * @return
     */
    List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists);

    /**
     * 发送文本消息
     * @param receiverId
     * @param msgType
     * @param content
     * @return
     */
    SendMessageResponseEntity sendTextMessage(long receiverId, Integer msgType, String content) throws IOException;

    /**
     * 发送图片消息
     * @param senderUid
     * @param msgType
     * @param url
     * @return
     */
    SendMessageResponseEntity sendImageMessage(long senderUid, Integer msgType, String url) throws IOException;

    /**
     * 将视频投稿至B站
     * @param videoUrl
     * @return
     * @throws IOException
     */
    SubmitVideoResponseEntity submitVideo(String videoUrl);
}
