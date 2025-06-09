package com.bilibili.chatbot.plus.domain.qwen.model.entity;

import com.bilibili.chatbot.plus.domain.qwen.model.valobj.CommandEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 命令请求实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandRequestEntity {
    private long senderUid;
    private String question;
    private CommandEnum commandEnum;
    private String baseUrl;

    public String getRequest(String question) {
        String command = commandEnum.getCommand();
        if (command == null || command.isEmpty()) {
            return question;
        }
        // 去文字水印/图像超分不用额外要求
        if (commandEnum.equals(CommandEnum.REMOVE_WATERMARK) || commandEnum.equals(CommandEnum.SUPER_RESOLUTION)) {
            return command;
        }
        return question.substring(command.length() + 1);
    }
}
