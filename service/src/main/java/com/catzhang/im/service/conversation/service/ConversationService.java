package com.catzhang.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catzhang.im.common.enums.ConversationType;
import com.catzhang.im.common.model.message.MessageReadedContent;
import com.catzhang.im.service.conversation.dao.ConversationSetEntity;
import com.catzhang.im.service.conversation.dao.mapper.ConversationSetMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author crazycatzhang
 */
@Service
public class ConversationService {

    @Autowired
    ConversationSetMapper conversationSetMapper;

    public String convertConversationId(Integer type, String fromId, String toId) {
        return type + "_" + fromId + "_" + toId;
    }

    public void messageMarkRead(MessageReadedContent messageReadedContent) {

        String toId = messageReadedContent.getToId();
        if (messageReadedContent.getConversationType() == ConversationType.GROUP.getCode()) {
            toId = messageReadedContent.getGroupId();
        }

        String conversationId = convertConversationId(messageReadedContent.getConversationType(), messageReadedContent.getFromId(), toId);
        QueryWrapper<ConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        query.eq("app_id", messageReadedContent.getAppId());
        ConversationSetEntity conversationSetEntity = conversationSetMapper.selectOne(query);
        if (conversationSetEntity == null) {
            conversationSetEntity = new ConversationSetEntity();
            conversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent, conversationSetEntity);
            conversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            conversationSetMapper.insert(conversationSetEntity);
        } else {
            conversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            conversationSetMapper.readMark(conversationSetEntity);
        }
    }

}
