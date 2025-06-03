package com.bilibili.chatbot.plus.domain.bilibili.model.valobj;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShareContent {
    private String author;
    private String headline;
    private long id;
    private int source;
    private String thumb;
    private String title;
    private String bvid;

    public String getVideoUrl(String bvid) {
        return "https://www.bilibili.com/video/" + bvid + "/?spm_id_from=333.1007.search-card.all.click&vd_source=795fb03bc8fe78dccf07b16bc66729ff";
    }

}
