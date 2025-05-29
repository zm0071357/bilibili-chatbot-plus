package com.bilibili.chatbot.plus.domain.bilibili.adapter.repository;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.qwen.model.QwenResponseEntity;

import java.io.IOException;
import java.util.List;

public interface BilibiliRepository {

    List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists);

    QwenResponseEntity handle(long userId, String question, String sign) throws IOException;

}
