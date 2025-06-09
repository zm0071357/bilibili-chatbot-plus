package com.bilibili.chatbot.plus.domain.qwen.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CommandEnum {
    CREATE_IMAGE("生成图片"),
    DESCRIPTION_EDIT("指令编辑"),
    REMOVE_WATERMARK("去文字水印"),
    EXPAND("扩图"),
    SUPER_RESOLUTION("图像超分"),
    COLORIZATION("图像上色"),
    CREATE_VIDEO("生成视频"),
    CREATE_VIDEO_WITH_BASE_IMAGE_URL("图生视频"),
    ;

    private String command;

    public static CommandEnum getCommand(String text) {
        for (CommandEnum command : CommandEnum.values()) {
            if (text.startsWith(command.getCommand())) {
                return command;
            }
        }
        return null;
    }
}
