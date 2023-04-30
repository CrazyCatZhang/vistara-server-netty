package com.catzhang.message.service;

import com.catzhang.im.common.model.message.MessageContent;
import com.catzhang.message.dao.MessageBodyEntity;
import com.catzhang.message.dao.MessageHistoryEntity;
import com.catzhang.message.dao.mapper.MessageBodyMapper;
import com.catzhang.message.dao.mapper.MessageHistoryMapper;
import com.catzhang.message.model.HandleStoreP2PMessageDto;
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
public class StoreMessageService {

    @Autowired
    MessageBodyMapper messageBodyMapper;

    @Autowired
    MessageHistoryMapper messageHistoryMapper;

    @Transactional
    public void handleStoreP2PMessage(HandleStoreP2PMessageDto handleStoreP2PMessageDto) {
        messageBodyMapper.insert(handleStoreP2PMessageDto.getMessageBodyEntity());
        List<MessageHistoryEntity> imMessageHistoryEntities = extractP2PMessageHistory(handleStoreP2PMessageDto.getMessageContent(), handleStoreP2PMessageDto.getMessageBodyEntity());
        messageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
    }

    public List<MessageHistoryEntity> extractP2PMessageHistory(MessageContent messageContent,
                                                               MessageBodyEntity messageBodyEntity) {
        List<MessageHistoryEntity> list = new ArrayList<>();
        MessageHistoryEntity fromHistory = new MessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(messageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());
        fromHistory.setSequence(messageContent.getMessageSequence());

        MessageHistoryEntity toHistory = new MessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(messageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());
        toHistory.setSequence(messageContent.getMessageSequence());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

}
