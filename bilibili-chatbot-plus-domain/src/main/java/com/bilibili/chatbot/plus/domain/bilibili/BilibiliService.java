package com.bilibili.chatbot.plus.domain.bilibili;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;

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
     * 处理
     * @param unHandleSessionLists 未回复对话列表
     * @return
     */
    void handle(List<SessionsEntity.Data.SessionList> unHandleSessionLists) throws IOException;

    /**
     * 发送消息
     * @param receiverId
     * @param msgType
     * @param content
     * @return
     * @throws IOException
     */
    SendMessageResponseEntity sendMessage(long receiverId, Integer msgType, String content) throws IOException;

}
