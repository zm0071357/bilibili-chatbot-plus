package com.bilibili.chatbot.plus.domain.qwen.adapter.repository;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.MessageContextEntity;
import com.bilibili.chatbot.plus.domain.qwen.model.entity.QwenResponseEntity;

import java.io.IOException;

public interface QwenRepository {

    /**
     * 处理普通文字消息
     * @param entity
     * @return
     */
    QwenResponseEntity handleTextMessage(MessageContextEntity entity);

    /**
     * 获取在线链接
     * @param url
     * @return
     * @throws IOException
     */
    String getOnlineLink(String url) throws IOException;

    /**
     * 处理图片消息
     * @param entity
     * @return
     */
    QwenResponseEntity handleImageMessage(MessageContextEntity entity);

    /**
     * 处理分享消息
     * @param entity
     * @return
     * @throws IOException
     */
    QwenResponseEntity handleVideoMessage(MessageContextEntity entity);

    /**
     * 生成图片
     * @param request
     * @return
     * @throws IOException
     */
    QwenResponseEntity createImage(String request) throws IOException;

    /**
     * 指令编辑
     * @param request
     * @param url
     * @return
     * @throws IOException
     */
    QwenResponseEntity descriptionEdit(String request, String url) throws IOException;

    /**
     * 去文字水印
     * @param request
     * @param url
     * @return
     * @throws IOException
     */
    QwenResponseEntity removeWatermark(String request, String url) throws IOException;

    /**
     * 扩图
     * @param request
     * @param url
     * @return
     * @throws IOException
     */
    QwenResponseEntity expand(String request, String url) throws IOException;

    /**
     * 图像超分
     * @param request
     * @param url
     * @return
     * @throws IOException
     */
    QwenResponseEntity superResolution(String request, String url) throws IOException;

    /**
     * 图像上色
     * @param request
     * @param url
     * @return
     * @throws IOException
     */
    QwenResponseEntity colorization(String request, String url) throws IOException;

    /**
     * 文生视频
     * @param request
     * @return
     */
    QwenResponseEntity createVideo(String request) throws IOException;

    /**
     * 图生视频 - 基于首帧
     * @param request
     * @param url
     * @return
     */
    QwenResponseEntity createVideoWithBaseImageUrl(String request, String url) throws IOException;
}
