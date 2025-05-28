package com.bilibili.chatbot.plus.domain.qwen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QwenResponseEntity {
    private String result;
    private boolean isImage;

}
