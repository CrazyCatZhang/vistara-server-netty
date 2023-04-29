package com.catzhang.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.DelFlag;
import com.catzhang.im.common.model.message.GroupMessageContent;
import com.catzhang.im.common.model.message.HandleStoreP2PMessageDto;
import com.catzhang.im.common.model.message.MessageBody;
import com.catzhang.im.common.model.message.MessageContent;
import com.catzhang.im.service.group.dao.GroupMessageHistoryEntity;
import com.catzhang.im.service.group.dao.mapper.GroupMessageHistoryMapper;
import com.catzhang.im.service.message.dao.MessageBodyEntity;
import com.catzhang.im.service.message.dao.MessageHistoryEntity;
import com.catzhang.im.service.message.dao.mapper.MessageBodyMapper;
import com.catzhang.im.service.message.dao.mapper.MessageHistoryMapper;
import com.catzhang.im.service.utils.SnowflakeIdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Autowired
    GroupMessageHistoryMapper groupMessageHistoryMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {

        MessageBody messageBody = extractMessageBody(messageContent);
        HandleStoreP2PMessageDto handleStoreP2PMessageDto = new HandleStoreP2PMessageDto();
        handleStoreP2PMessageDto.setMessageContent(messageContent);
        handleStoreP2PMessageDto.setMessageBody(messageBody);
        messageContent.setMessageKey(messageBody.getMessageKey());
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.STOREPTOPMESSAGE, "", JSONObject.toJSONString(handleStoreP2PMessageDto));

    }

    public MessageBody extractMessageBody(MessageContent messageContent) {
        MessageBody messageBody = new MessageBody();
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

    @Transactional
    public void storeGroupMessage(GroupMessageContent messageContent) {
//        MessageBodyEntity messageBody = extractMessageBody(messageContent);
//        messageBodyMapper.insert(messageBody);
//        GroupMessageHistoryEntity groupMessageHistoryEntity = extractGroupMessageHistory(messageContent, messageBody);
//        groupMessageHistoryMapper.insert(groupMessageHistoryEntity);
//        messageContent.setMessageKey(messageBody.getMessageKey());
    }

    private GroupMessageHistoryEntity extractGroupMessageHistory(GroupMessageContent
                                                                         messageContent, MessageBodyEntity messageBodyEntity) {
        GroupMessageHistoryEntity result = new GroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

}
