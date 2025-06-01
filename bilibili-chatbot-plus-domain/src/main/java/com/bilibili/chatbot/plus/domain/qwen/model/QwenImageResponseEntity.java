package com.bilibili.chatbot.plus.domain.qwen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.MultipartBody;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QwenImageResponseEntity {

    private String url;
    private MultipartBody.Part part;

}
