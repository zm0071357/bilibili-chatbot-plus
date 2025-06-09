package com.bilibili.chatbot.plus.domain.bilibili.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
public class SessionsEntity {
    private int code;
    private String msg;
    private String message;
    private int ttl;
    private Data data;

    @lombok.Data
    public static class Data {
        @JsonProperty("session_list")
        private List<SessionList> sessionList;
        private int has_more;
        private boolean anti_disturb_cleaning;
        private int is_address_list_empty;
        @JsonProperty("system_msg")
        private SystemMsg systemMsg;
        private boolean show_level;

        @lombok.Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SessionList {
            private long talker_id;
            private int session_type;
            private int at_seqno;
            private long top_ts;
            private String group_name;
            private String group_cover;
            private int is_follow;
            private int is_dnd;
            private long ack_seqno;
            private long ack_ts;
            private long session_ts;
            private int unread_count;
            @JsonProperty("last_msg")
            private LastMsg lastMsg;
            private int group_type;
            private int can_fold;
            private int status;
            private long max_seqno;
            private int new_push_msg;
            private int setting;
            private int is_guardian;
            private int is_intercept;
            private int is_trust;
            private int system_msg_type;
            private int live_status;
            private int biz_msg_unread_count;
            private String user_label;

            @lombok.Data
            public static class LastMsg {
                private long sender_uid;
                private int receiver_type;
                private long receiver_id;
                private int msg_type;
                private String content;
                private long msg_seqno;
                private long timestamp;
                private String at_uids;
                private long msg_key;
                private int msg_status;
                private String notify_code;
                private int msg_source;
                private int new_face_version;
            }
        }

        @lombok.Data
        public static class SystemMsg {
            @JsonProperty("7")
            private Long num;
        }
    }

}
