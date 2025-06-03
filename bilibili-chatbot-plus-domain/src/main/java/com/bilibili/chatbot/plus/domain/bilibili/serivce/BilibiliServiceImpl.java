package com.bilibili.chatbot.plus.domain.bilibili.serivce;

import com.bilibili.chatbot.plus.domain.bilibili.BilibiliService;
import com.bilibili.chatbot.plus.domain.bilibili.adapter.repository.BilibiliRepository;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SendMessageResponseEntity;
import com.bilibili.chatbot.plus.domain.bilibili.model.entity.SessionsEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class BilibiliServiceImpl implements BilibiliService {

    @Resource
    private BilibiliRepository bilibiliRepository;

    @Override
    public SessionsEntity getSessions() throws IOException {
        return bilibiliRepository.getSessions();
    }

    @Override
    public List<SessionsEntity.Data.SessionList> getUnHandleSessionLists(List<SessionsEntity.Data.SessionList> sessionLists) {
        return bilibiliRepository.getUnHandleSessionLists(sessionLists);
    }

    @Override
    public SendMessageResponseEntity sendTextMessage(long receiverId, Integer msgType, String content) throws IOException {
        return bilibiliRepository.sendTextMessage(receiverId, msgType, content);
    }

    @Override
    public SendMessageResponseEntity sendImageMessage(long senderUid, Integer msgType, String url) throws IOException {
        return bilibiliRepository.sendImageMessage(senderUid, msgType, url);
    }

}
