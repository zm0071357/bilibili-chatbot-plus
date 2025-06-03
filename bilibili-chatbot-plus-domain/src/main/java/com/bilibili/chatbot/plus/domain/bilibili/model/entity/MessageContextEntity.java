package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.ShareContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageContextEntity {
    private long senderUid;
    private String question;
    private Integer msgType;
    private ShareContent shareContent;

}
