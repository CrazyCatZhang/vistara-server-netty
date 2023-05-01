package com.catzhang.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catzhang.im.codec.pack.conversation.DeleteConversationPack;
import com.catzhang.im.codec.pack.conversation.UpdateConversationPack;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ConversationErrorCode;
import com.catzhang.im.common.enums.ConversationType;
import com.catzhang.im.common.enums.command.ConversationEventCommand;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.common.model.message.MessageReadedContent;
import com.catzhang.im.service.conversation.dao.ConversationSetEntity;
import com.catzhang.im.service.conversation.dao.mapper.ConversationSetMapper;
import com.catzhang.im.service.conversation.model.DeleteConversationReq;
import com.catzhang.im.service.conversation.model.UpdateConversationReq;
import com.catzhang.im.service.sequence.RedisSequence;
import com.catzhang.im.service.utils.MessageProducer;
import com.catzhang.im.service.utils.WriteUserSequence;
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

    @Autowired
    AppConfig appConfig;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSequence redisSequence;

    @Autowired
    WriteUserSequence writeUserSequence;

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
        long sequence = redisSequence.getSequence(messageReadedContent.getAppId() + ":" + Constants.SequenceConstants.CONVERSATION);

        if (conversationSetEntity == null) {
            conversationSetEntity = new ConversationSetEntity();
            conversationSetEntity.setSequence(sequence);
            conversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent, conversationSetEntity);
            conversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            conversationSetMapper.insert(conversationSetEntity);
        } else {
            conversationSetEntity.setSequence(sequence);
            conversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            conversationSetMapper.readMark(conversationSetEntity);
        }

        writeUserSequence.writeUserSequence(messageReadedContent.getAppId(),
                messageReadedContent.getFromId(), Constants.SequenceConstants.CONVERSATION, sequence);
    }

    public ResponseVO deleteConversation(DeleteConversationReq req) {
        QueryWrapper<ConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", req.getConversationId());
        queryWrapper.eq("app_id", req.getAppId());
        ConversationSetEntity conversationSetEntity = conversationSetMapper.selectOne(queryWrapper);
        if (conversationSetEntity != null) {
            conversationSetEntity.setIsMute(0);
            conversationSetEntity.setIsTop(0);
            conversationSetMapper.update(conversationSetEntity, queryWrapper);
        }

        if (appConfig.getDeleteConversationSyncMode() == 1) {
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(),
                            req.getImei()));
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO updateConversation(UpdateConversationReq req) {

        if (req.getIsTop() == null && req.getIsMute() == null) {
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }

        QueryWrapper<ConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", req.getConversationId());
        queryWrapper.eq("app_id", req.getAppId());
        ConversationSetEntity conversationSetEntity = conversationSetMapper.selectOne(queryWrapper);
        if (conversationSetEntity != null) {

            long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.CONVERSATION);

            if (req.getIsMute() != null) {
                conversationSetEntity.setIsTop(req.getIsTop());
            }
            if (req.getIsMute() != null) {
                conversationSetEntity.setIsMute(req.getIsMute());
            }

            conversationSetEntity.setSequence(sequence);

            conversationSetMapper.update(conversationSetEntity, queryWrapper);

            writeUserSequence.writeUserSequence(req.getAppId(), req.getFromId(),
                    Constants.SequenceConstants.CONVERSATION, sequence);

            //TODO: 会话更新TCP通知
            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(conversationSetEntity.getIsMute());
            pack.setIsTop(conversationSetEntity.getIsTop());
            pack.setSequence(sequence);
            pack.setConversationType(conversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(),
                            req.getImei()));
        }

        return ResponseVO.successResponse();
    }

}
