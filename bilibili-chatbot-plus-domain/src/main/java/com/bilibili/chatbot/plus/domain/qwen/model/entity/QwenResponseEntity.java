package com.bilibili.chatbot.plus.domain.qwen.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QwenResponseEntity<E> {
    private E result;
    private boolean isImage;

}
