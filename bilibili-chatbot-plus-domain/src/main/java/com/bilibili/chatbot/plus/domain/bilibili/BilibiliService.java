package com.bilibili.chatbot.plus.domain.bilibili;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SubmitVideoResponseEntity;

import java.io.IOException;
import java.util.List;

public interface BilibiliService {

    /**
     * 获取 Session
     * @return
     */
    SessionsEntity getSessions() throws IOException;

    /**
     * 获取未回复对话列表
     * @param sessionLists 对话列表
     * @return
     */
    List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists);

    /**
     * 发送文字消息
     * @param receiverId
     * @param msgType
     * @param content
     * @return
     * @throws IOException
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


    SubmitVideoResponseEntity uploadVideo(String videoUrl);
}
