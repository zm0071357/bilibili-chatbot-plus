package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubTitleEntity {
    private Integer open;
    private String lan;
}
