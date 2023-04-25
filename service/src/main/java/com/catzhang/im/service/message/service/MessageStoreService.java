package com.catzhang.im.service.message.service;

import com.catzhang.im.common.enums.DelFlag;
import com.catzhang.im.common.model.message.MessageContent;
import com.catzhang.im.service.message.dao.MessageBodyEntity;
import com.catzhang.im.service.message.dao.MessageHistoryEntity;
import com.catzhang.im.service.message.dao.mapper.MessageBodyMapper;
import com.catzhang.im.service.message.dao.mapper.MessageHistoryMapper;
import com.catzhang.im.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class MessageStoreService {

    @Autowired
    MessageHistoryMapper messageHistoryMapper;

    @Autowired
    MessageBodyMapper messageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {

        MessageBodyEntity messageBody = extractMessageBody(messageContent);
        messageBodyMapper.insert(messageBody);
        List<MessageHistoryEntity> messageHistoryEntities = extractP2PMessageHistory(messageContent, messageBody);
        messageHistoryMapper.insertBatchSomeColumn(messageHistoryEntities);
        messageContent.setMessageKey(messageBody.getMessageKey());
    }

    public MessageBodyEntity extractMessageBody(MessageContent messageContent) {
        MessageBodyEntity messageBody = new MessageBodyEntity();
        messageBody.setAppId(messageContent.getAppId());
//        messageBody.setMessageKey(SnowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlag.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    public List<MessageHistoryEntity> extractP2PMessageHistory(MessageContent messageContent,
                                                               MessageBodyEntity messageBodyEntity) {
        List<MessageHistoryEntity> list = new ArrayList<>();
        MessageHistoryEntity fromHistory = new MessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(messageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        MessageHistoryEntity toHistory = new MessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(messageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

}
