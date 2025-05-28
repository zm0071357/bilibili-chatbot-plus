package com.bilibili.chatbot.plus.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import com.bilibili.chatbot.plus.domain.qwen.model.QwenResponseEntity;
import lombok.extern.slf4j.Slf4j;
import qwen.sdk.largemodel.chat.enums.ChatModelEnum;
import qwen.sdk.largemodel.chat.impl.ChatServiceImpl;
import qwen.sdk.largemodel.chat.model.ChatMutiResponse;
import qwen.sdk.largemodel.chat.model.ChatRequest;
import qwen.sdk.largemodel.chat.model.ChatResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class BilibiliRepositoryImpl implements BilibiliRepository{

    private final long loginId;

    private final ChatServiceImpl chatServiceImpl;

    private Map<Long, List<ChatRequest.Input.Message>> history = new HashMap<>();


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
    public QwenResponseEntity handle(long userId, String question) throws IOException {
        List<ChatRequest.Input.Message> messages = getHistory(userId);
        messages.add(ChatRequest.Input.Message.builder()
                .role("user")
                .content(question)
                .build());
        ChatRequest request = ChatRequest.builder()
                .model(ChatModelEnum.QWEN_VL_PLUS.getModel())
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
        ChatMutiResponse response = chatServiceImpl.chatWithMultimodal(request);
        String result = String.valueOf(response.getOutput().getChoices().get(0).getMessage().getContent().get(0).getText());
        log.info("返回结果:{}", JSON.toJSONString(response));
        // 添加历史记录
        // 后续调用失败的话，将message删除
        messages.add(ChatRequest.Input.Message.builder()
                .role("system")
                .content(result)
                .build());
        // 更新
        history.put(userId, messages);
        return QwenResponseEntity.builder()
                .result(result)
                .isImage(false)
                .build();
    }

    /**
     * 根据用户ID获取历史记录
     * @param userId
     * @return
     */
    public List<ChatRequest.Input.Message> getHistory(long userId) {
        return history.getOrDefault(userId, defaultHistory(userId));
    }

    /**
     * 创建初始记录
     * @param userId
     * @return
     */
    private List<ChatRequest.Input.Message> defaultHistory(long userId) {
        log.info("用户初次对话，创建历史记录:{}", userId);
        List<ChatRequest.Input.Message> messages = new ArrayList<>();
        messages.add(ChatRequest.Input.Message.builder()
                .role("system")
                .content(MessageConstant.DEFAULT_MESSAGE)
                .build());
        return messages;
    }

}