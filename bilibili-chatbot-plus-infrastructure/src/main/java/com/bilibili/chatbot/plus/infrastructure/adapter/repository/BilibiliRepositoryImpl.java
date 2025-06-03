package com.bilibili.chatbot.plus.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliImagePort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendImageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BilibiliRepositoryImpl implements BilibiliRepository{
    private final BilibiliPort bilibiliPort;
    private final BilibiliImagePort bilibiliImagePort;
    private final long loginId;
    private final String cookie;
    private final String csrf;
    private final Integer sessionType;
    private final Integer size;
    private final String mobiApp;
    private final Integer receiverType;

    public BilibiliRepositoryImpl(BilibiliPort bilibiliPort, BilibiliImagePort bilibiliImagePort, long loginId, String cookie, String csrf, Integer sessionType, Integer size, String mobiApp, Integer receiverType) {
        this.bilibiliPort = bilibiliPort;
        this.bilibiliImagePort = bilibiliImagePort;
        this.loginId = loginId;
        this.cookie = cookie;
        this.csrf = csrf;
        this.sessionType = sessionType;
        this.size = size;
        this.mobiApp = mobiApp;
        this.receiverType = receiverType;
    }

    @Override
    public SessionsEntity getSessions() throws IOException {
        Call<SessionsEntity> call = bilibiliPort.getSessions(cookie, sessionType, size, mobiApp);
        Response<SessionsEntity> response = call.execute();
        return response.body();
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return sessionLists.stream()
                .filter(session -> session.getLastMsg() != null && session.getLastMsg().getSender_uid() != loginId)
                .collect(Collectors.toList());
    }

    @Override
    public SendMessageResponseEntity sendTextMessage(long receiverId, Integer msgType, String content) throws IOException {
        String devId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        Call<SendMessageResponseEntity> call = bilibiliPort.sendMessage(cookie, loginId, receiverId, receiverType, msgType,
                devId, timestamp, MessageConstant.getContent(content), csrf, csrf);
        Response<SendMessageResponseEntity> response = call.execute();
        return response.body();
    }

    @Override
    public SendMessageResponseEntity sendImageMessage(long senderUid, Integer msgType, String url) throws IOException {
        // 将图片上传到b站上
        MultipartBody.Part fileUp = getFileUp(url);
        Call<SendImageResponseEntity> sendImageCall = bilibiliImagePort.sendImage(cookie, fileUp, MessageConstant.BIZ, csrf);
        Response<SendImageResponseEntity> sendImageResponse = sendImageCall.execute();
        SendImageResponseEntity imageResponseEntity = sendImageResponse.body();
        log.info("imageResponseEntity:{}", JSON.toJSONString(imageResponseEntity));
        // 发送b站图片消息
        String devId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        Call<SendMessageResponseEntity> SendMessageCall = bilibiliPort.sendMessage(cookie, loginId, senderUid, receiverType, msgType, devId, timestamp,
                MessageConstant.getImageContent(imageResponseEntity.getData().getImageUrl(), imageResponseEntity.getData().getImageHeight(),
                        imageResponseEntity.getData().getImageWidth(), imageResponseEntity.getData().getImgSize()), csrf, csrf);
        Response<SendMessageResponseEntity> response = SendMessageCall.execute();
        return response.body();
    }

    /**
     * 构造Part参数
     * @param fileUrl 大模型生成的图片url
     * @return
     * @throws IOException
     */
    private MultipartBody.Part getFileUp(String fileUrl) throws IOException {
        try (InputStream in = new URL(fileUrl).openStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = out.toByteArray();
            RequestBody fileRequestBody = RequestBody.create(
                    MediaType.parse("image/png"),
                    imageBytes
            );
            return MultipartBody.Part.createFormData(
                    "file_up",
                    "image.png",
                    fileRequestBody);
        }
    }

}