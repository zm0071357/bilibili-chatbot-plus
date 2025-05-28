package com.bilibili.chatbot.plus.infrastructure.adapter.repository;

import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.qwen.model.QwenResponseEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BilibiliRepositoryImpl implements BilibiliRepository{

    private final long loginId;

    public BilibiliRepositoryImpl(long loginId) {
        this.loginId = loginId;
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return sessionLists.stream()
                .filter(session -> session.getLastMsg() != null && session.getLastMsg().getSender_uid() != loginId)
                .collect(Collectors.toList());
    }

    @Override
    public QwenResponseEntity handle(String question) {
        return null;
    }

}