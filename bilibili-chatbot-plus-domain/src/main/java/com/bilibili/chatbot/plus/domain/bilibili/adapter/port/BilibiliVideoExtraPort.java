package com.bilibili.chatbot.plus.domain.bilibili.adapter.port;

import com.bilibili.chatbot.plus.domain.bilibili.model.entity.EndUploadVideoEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.UploadMetadataResponseEntity;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;


public interface BilibiliVideoExtraPort {

    /**
     * 上传视频元数据
     * @param url
     * @param cookie
     * @param auth
     * @param output
     * @return
     */
    @POST
    Call<UploadMetadataResponseEntity> uploadMetadata(@Url String url,
                                                      @Header("Cookie") String cookie,
                                                      @Header("X-Upos-Auth") String auth,
                                                      @Query("output") String output);

    /**
     * 分片上传视频文件
     * @param url
     * @param cookie
     * @param auth
     * @param partNumber
     * @param uploadId
     * @param chunk
     * @param chunks
     * @param size
     * @param start
     * @param end
     * @param total
     * @param videoChunk
     * @return
     */
    @PUT
    @Headers("Content-Type: application/octet-stream")
    Call<ResponseBody> fragmentUpload(@Url String url,
                                      @Header("Cookie") String cookie,
                                      @Header("X-Upos-Auth") String auth,
                                      @Query("partNumber") Integer partNumber,
                                      @Query("uploadId") String uploadId,
                                      @Query("chunk") Integer chunk,
                                      @Query("chunks") Integer chunks,
                                      @Query("size") long size,
                                      @Query("start") long start,
                                      @Query("end") long end,
                                      @Query("total") long total,
                                      @Body RequestBody videoChunk);

    /**
     * 结束上传视频文件
     * @param url
     * @param auth
     * @param output
     * @param name
     * @param profile
     * @param uploadId
     * @param bizId
     * @param parts
     * @return
     */
    @POST
    @Headers("Content-Type: application/json")
    Call<EndUploadVideoEntity> endUploadVideo(@Url String url,
                                              //@Header("Cookie") String cookie,
                                              @Header("X-Upos-Auth") String auth,
                                              @Query("output") String output,
                                              @Query("name") String name,
                                              @Query("profile") String profile,
                                              @Query("uploadId") String uploadId,
                                              @Query("biz_id") long bizId,
                                              @Body RequestBody parts);

}
