package com.bilibili.chatbot.plus.domain.qwen;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.MessageContextEntity;
import com.bilibili.chatbot.plus.domain.qwen.model.entity.QwenResponseEntity;

public interface QwenService {

    /**
     * 处理消息
     * @param messageContextEntity
     * @return
     */
    QwenResponseEntity handle(MessageContextEntity messageContextEntity);

}
