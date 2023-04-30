package com.catzhang.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ConversationType;
import com.catzhang.im.common.enums.DelFlag;
import com.catzhang.im.common.model.message.*;
import com.catzhang.im.service.conversation.service.ConversationService;
import com.catzhang.im.service.group.dao.GroupMessageHistoryEntity;
import com.catzhang.im.service.group.dao.mapper.GroupMessageHistoryMapper;
import com.catzhang.im.service.message.dao.MessageBodyEntity;
import com.catzhang.im.service.message.dao.MessageHistoryEntity;
import com.catzhang.im.service.message.dao.mapper.MessageBodyMapper;
import com.catzhang.im.service.message.dao.mapper.MessageHistoryMapper;
import com.catzhang.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AppConfig appConfig;

    @Autowired
    ConversationService conversationService;

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
        messageBody.setMessageKey(SnowflakeIdWorker.nextId());
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
        MessageBody messageBody = extractMessageBody(messageContent);
        HandleStoreGroupMessageDto handleStoreGroupMessageDto = new HandleStoreGroupMessageDto();
        handleStoreGroupMessageDto.setGroupMessageContent(messageContent);
        handleStoreGroupMessageDto.setMessageBody(messageBody);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.STOREGROUPMESSAGE,
                "",
                JSONObject.toJSONString(handleStoreGroupMessageDto));
        messageContent.setMessageKey(messageBody.getMessageKey());
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

    public void setMessageFromMessageIdCache(Integer appId, String messageId, Object messageContent) {
        String key = appId + ":" + Constants.RedisConstants.CACHEMESSAGE + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(messageContent), 300, TimeUnit.SECONDS);
    }

    public <T> T getMessageFromMessageIdCache(Integer appId, String messageId, Class<T> clazz) {
        //appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.CACHEMESSAGE + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }

    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {
        // 找到fromId的队列
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OFFLINEMESSAGE + ":" + offlineMessage.getFromId();
        // 找到toId的队列
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OFFLINEMESSAGE + ":" + offlineMessage.getToId();

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        if (operations.zCard(fromKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(fromKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationType.P2P.getCode(), offlineMessage.getFromId(), offlineMessage.getToId()
        ));
        // 插入 数据 根据messageKey 作为分值
        operations.add(fromKey, JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());

        //判断 队列中的数据是否超过设定值
        if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(toKey, 0, 0);
        }

        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationType.P2P.getCode(), offlineMessage.getToId(), offlineMessage.getFromId()
        ));
        // 插入 数据 根据messageKey 作为分值
        operations.add(toKey, JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());
    }

    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage, List<String> memberIds) {

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        offlineMessage.setConversationType(ConversationType.GROUP.getCode());

        for (String memberId : memberIds) {
            // 找到toId的队列
            String toKey = offlineMessage.getAppId() + ":" +
                    Constants.RedisConstants.OFFLINEMESSAGE + ":" +
                    memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationType.GROUP.getCode(), memberId, offlineMessage.getToId()
            ));
            if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
                operations.removeRange(toKey, 0, 0);
            }
            // 插入 数据 根据messageKey 作为分值
            operations.add(toKey, JSONObject.toJSONString(offlineMessage),
                    offlineMessage.getMessageKey());
        }

    }

}
