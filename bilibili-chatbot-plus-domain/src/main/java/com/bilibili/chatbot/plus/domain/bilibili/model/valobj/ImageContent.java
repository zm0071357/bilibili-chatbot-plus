package com.bilibili.chatbot.plus.domain.bilibili.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageContent {

    private Integer height;
    private Integer width;
    private String imageType;
    private Integer original;
    private Integer size;
    private String url;

}
