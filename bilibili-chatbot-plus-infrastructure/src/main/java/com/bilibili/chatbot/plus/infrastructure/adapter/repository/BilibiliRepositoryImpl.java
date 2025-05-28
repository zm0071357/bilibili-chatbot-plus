package com.bilibili.chatbot.plus.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.Content;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import com.bilibili.chatbot.plus.domain.qwen.model.QwenResponseEntity;
import lombok.extern.slf4j.Slf4j;
import qwen.sdk.largemodel.chat.enums.ChatModelEnum;
import qwen.sdk.largemodel.chat.impl.ChatServiceImpl;
import qwen.sdk.largemodel.chat.model.ChatRequest;
import qwen.sdk.largemodel.chat.model.ChatResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BilibiliRepositoryImpl implements BilibiliRepository{

    private final long loginId;

    private final ChatServiceImpl chatServiceImpl;

    public BilibiliRepositoryImpl(long loginId, ChatServiceImpl chatServiceImpl) {
        this.loginId = loginId;
        this.chatServiceImpl = chatServiceImpl;
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return sessionLists.stream()
                .filter(session -> session.getLastMsg() != null && session.getLastMsg().getSender_uid() != loginId)
                .collect(Collectors.toList());
    }

    @Override
    public QwenResponseEntity handle(String question) throws IOException {
        // 构造请求参数
        List<ChatRequest.Input.Message> messages = new ArrayList<>();
        messages.add(ChatRequest.Input.Message.builder()
                .role("system")
                .content(MessageConstant.DEFAULT_MESSAGE)
                .build());
        messages.add(ChatRequest.Input.Message.builder()
                .role("user")
                .content(question)
                .build());
        ChatRequest request = ChatRequest.builder()
                .model(ChatModelEnum.QWEN_PLUS.getModel())
                .input(ChatRequest.Input.builder()
                        .messages(messages)
                        .build())
                .parameters(ChatRequest.Parameters.builder()
                        .resultFormat("message")
                        .enableSearch(true)
                        .searchOptions(ChatRequest.Parameters.SearchOptions.builder()
                                .enableSource(true)
                                .forcedSearch(true)
                                .build())
                        .build())
                .build();
        log.info("请求参数:{}", JSON.toJSONString(request));
        // 发起请求
        ChatResponse response = chatServiceImpl.chat(request);
        log.info("返回结果:{}", JSON.toJSONString(response));
        return QwenResponseEntity.builder()
                .result(String.valueOf(response.getOutput().getChoices().get(0).getMessage().getContent()))
                .isImage(false)
                .build();
    }

}