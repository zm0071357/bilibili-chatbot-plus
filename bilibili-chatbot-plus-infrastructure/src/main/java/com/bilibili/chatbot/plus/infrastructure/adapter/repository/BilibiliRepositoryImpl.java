package com.bilibili.chatbot.plus.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliImagePort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliVideoExtraPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.port.BilibiliVideoPort;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.*;
import com.bilibili.chatbot.plus.domain.bilibili.model.valobj.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Call;
import retrofit2.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BilibiliRepositoryImpl implements BilibiliRepository{

    @Value("${bilibili.chatbot.plus.config.cover}")
    private String cover;
    private final BilibiliPort bilibiliPort;
    private final BilibiliImagePort bilibiliImagePort;
    private final BilibiliVideoPort bilibiliVideoPort;
    private final BilibiliVideoExtraPort bilibiliVideoExtraPort;
    private final long loginId;
    private final String cookie;
    private final String csrf;
    private final Integer sessionType;
    private final Integer size;
    private final String mobiApp;
    private final Integer receiverType;
    private final OkHttpClient okHttpClient;

    public BilibiliRepositoryImpl(BilibiliPort bilibiliPort, BilibiliImagePort bilibiliImagePort, BilibiliVideoPort bilibiliVideoPort, BilibiliVideoExtraPort bilibiliVideoExtraPort,
                                  long loginId, String cookie, String csrf, Integer sessionType, Integer size, String mobiApp, Integer receiverType, OkHttpClient okHttpClient) {
        this.bilibiliPort = bilibiliPort;
        this.bilibiliImagePort = bilibiliImagePort;
        this.bilibiliVideoPort = bilibiliVideoPort;
        this.bilibiliVideoExtraPort = bilibiliVideoExtraPort;
        this.loginId = loginId;
        this.cookie = cookie;
        this.csrf = csrf;
        this.sessionType = sessionType;
        this.size = size;
        this.mobiApp = mobiApp;
        this.receiverType = receiverType;
        this.okHttpClient = okHttpClient;
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
        Call<SendImageResponseEntity> sendImageCall = bilibiliImagePort.sendImage(cookie, fileUp, "im", csrf);
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

    @Override
    public SubmitVideoResponseEntity submitVideo(String videoUrl) {
        try {
            // 上传封面图
            String coverBase64 = urlToBase64(cover);
            Call<SendCoverResponseEntity> call1 = bilibiliVideoPort.sendCover(cookie, csrf, coverBase64, System.currentTimeMillis());
            Response<SendCoverResponseEntity> response1 = call1.execute();
            SendCoverResponseEntity sendCoverResponseEntity = response1.body();
            String sendCover = sendCoverResponseEntity.getData().getUrl();
            log.info("上传封面图结果: sendCover:{}", sendCover);

            // 获取上传元数据 (预上传)
            String name = videoUrl;
            Call<PreUploadResponseEntity> call2 = bilibiliVideoPort.preUpload(cookie, name, "upos", "ugcupos/bup");
            Response<PreUploadResponseEntity> response2 = call2.execute();
            PreUploadResponseEntity preUploadResponseEntity = response2.body();
            log.info("获取上传元数据 (预上传)结果: preUploadResponseEntity:{}", JSON.toJSONString(preUploadResponseEntity));

            // 上传视频元数据
            long bizId = preUploadResponseEntity.getBiz_id();
            String auth = preUploadResponseEntity.getAuth();
            String url1 = preUploadResponseEntity.getUploadMetadataUrl(preUploadResponseEntity.getEndpoint(), preUploadResponseEntity.getUpos_uri());
            File file = getFile(videoUrl);
            log.info("上传视频元数据url:{}", url1);
            Call<UploadMetadataResponseEntity> call3 = bilibiliVideoExtraPort.uploadMetadata(url1, cookie, auth, "json");
            Response<UploadMetadataResponseEntity> response3 = call3.execute();
            UploadMetadataResponseEntity uploadMetadataResponseEntity = response3.body();
            log.info("上传视频元数据结果: uploadMetadataResponseEntity:{}", JSON.toJSONString(uploadMetadataResponseEntity));

            // 分片上传视频文件
            String url2 = preUploadResponseEntity.getFragmentUploadUrl(preUploadResponseEntity.getEndpoint(), preUploadResponseEntity.getUpos_uri());
            String uploadId = uploadMetadataResponseEntity.getUpload_id();
            RequestBody videoChunk = RequestBody.create(file, MediaType.parse("application/octet-stream"));
            Call<ResponseBody> call4 = bilibiliVideoExtraPort.fragmentUpload(url2, cookie, auth, 1, uploadId,0, 1, getOnlineFileSize(videoUrl), 0, getOnlineFileSize(videoUrl), getOnlineFileSize(videoUrl), videoChunk);
            Response<ResponseBody> response4 = call4.execute();
            String fragmentUploadResult = response4.body().string();
            log.info("分片上传视频文件结果:{}", fragmentUploadResult);

            // 结束上传视频文件
            List<PartEntity.Part> parts = new ArrayList<>();
            parts.add(PartEntity.Part.builder()
                    .partNumber(1)
                    .eTag("etag")
                    .build());
            PartEntity partEntity = PartEntity.builder()
                    .parts(parts)
                    .build();
            RequestBody partsBody = RequestBody.create(JSON.toJSONString(partEntity), MediaType.parse("application/json"));
            Call<EndUploadVideoEntity> call5 = bilibiliVideoExtraPort.endUploadVideo(url2, auth, "json", name, "ugcupos/bup", uploadId, bizId, partsBody);
            Response<EndUploadVideoEntity> response5 = call5.execute();
            EndUploadVideoEntity endUploadVideoEntity = response5.body();
            log.info("结束上传视频文件结果:{}", JSON.toJSONString(endUploadVideoEntity));

            // 投递视频稿件
            String fileName = endUploadVideoEntity.getFileName(endUploadVideoEntity.getKey());
            List<VideoEntity.Video> videoList = new ArrayList<>();
            videoList.add(VideoEntity.Video.builder()
                    .title("1")
                    .filename(fileName)
                    .desc("")
                    .cid(bizId)
                    .build());
            SendVideoRequestEntity sendVideoRequestEntity = SendVideoRequestEntity.builder()
                    .videos(videoList)
                    .cover(cover)
                    .cover43(cover)
                    .title("林薯条生成的视频")
                    .copyright(1)
                    .tid(21)
                    .tag("生活")
                    .desc_format_id(9999)
                    .desc("这是林薯条生成的视频哦")
                    .recreate(-1)
                    .dynamic("")
                    .interactive(0)
                    .act_reserve_create(0)
                    .ai_cover(0)
                    .no_disturbance(0)
                    .no_reprint(1)
                    .subtitle(SubTitleEntity.builder()
                            .open(0)
                            .lan("")
                            .build())
                    .dobly(0)
                    .lossless_music(0)
                    .up_close_danmu(false)
                    .up_close_reply(false)
                    .up_selection_reply(false)
                    .web_os(1)
                    .build();
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(sendVideoRequestEntity));
            log.info("投递视频稿件参数:{}", JSON.toJSONString(sendVideoRequestEntity));
            Call<SendVideoResponseEntity> call6 = bilibiliVideoPort.sendVideo(cookie, System.currentTimeMillis(), csrf, requestBody);
            Response<SendVideoResponseEntity> response6 = call6.execute();
            SendVideoResponseEntity sendVideoResponseEntity = response6.body();
            log.info("投递视频稿件结果:{}", JSON.toJSONString(sendVideoResponseEntity));

            // 删除临时文件
            file.deleteOnExit();
            return SubmitVideoResponseEntity.builder()
                    .result(sendVideoResponseEntity.getData().getBvid())
                    .isSuccess(true)
                    .build();
        } catch (IOException e) {
            log.info("视频投稿至B站出错:{}", e.getMessage());
            return SubmitVideoResponseEntity.builder()
                    .result(MessageConstant.UPLOAD_VIDEO_FAILED_MESSAGE)
                    .isSuccess(false)
                    .build();
        }
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

    /**
     * 将图片url转为Base64编码
     * @param cover
     * @return
     * @throws IOException
     */
    private String urlToBase64(String cover) throws IOException {
        // 从URL读取封面文件
        URL url = new URL(cover);
        InputStream inputStream = url.openStream();
        byte[] coverBytes = inputStream.readAllBytes(); // 读取字节流
        inputStream.close();
        // 使用 Base64 编码
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(coverBytes);
    }

    /**
     * 获取在线文件字节大小
     * @return
     * @throws IOException
     */
    private long getOnlineFileSize(String videoUrl) throws IOException {
        URL url = new URL(videoUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        long fileSize = conn.getContentLengthLong();
        log.info("文件字节大小: {},{}", videoUrl, fileSize);
        return fileSize;
    }

    /**
     * 下载在线文件到临时文件
     * @param videoUrl
     * @return
     * @throws IOException
     */
    private File getFile(String videoUrl) throws IOException {
        File tempFile = File.createTempFile("video", ".mp4");
        Request request = new Request.Builder().url(videoUrl).build();
        try (okhttp3.Response response = okHttpClient.newCall(request).execute()) {
            // 将网络文件写入本地临时文件
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                out.write(response.body().bytes());
            }
        }
        return tempFile;
    }

}