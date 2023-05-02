package com.catzhang.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.catzhang.im.codec.pack.message.MessageReadedPack;
import com.catzhang.im.codec.pack.message.RecallMessageNotifyPack;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ConversationType;
import com.catzhang.im.common.enums.DelFlag;
import com.catzhang.im.common.enums.MessageErrorCode;
import com.catzhang.im.common.enums.command.Command;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.enums.command.MessageCommand;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
import com.catzhang.im.common.model.message.MessageReadedContent;
import com.catzhang.im.common.model.message.MessageReceiveAckContent;
import com.catzhang.im.common.model.message.OfflineMessageContent;
import com.catzhang.im.common.model.message.RecallMessageContent;
import com.catzhang.im.service.conversation.service.ConversationService;
import com.catzhang.im.service.group.model.req.GetGroupMemberIdReq;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.message.dao.MessageBodyEntity;
import com.catzhang.im.service.message.dao.mapper.MessageBodyMapper;
import com.catzhang.im.service.sequence.RedisSequence;
import com.catzhang.im.service.utils.ConversationIdGenerate;
import com.catzhang.im.service.utils.GroupMessageProducer;
import com.catzhang.im.service.utils.MessageProducer;
import com.catzhang.im.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author crazycatzhang
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ConversationService conversationService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MessageBodyMapper messageBodyMapper;

    @Autowired
    RedisSequence redisSequence;

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {
        messageProducer.sendToUser(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK, messageReceiveAckContent, messageReceiveAckContent.getAppId());
    }

    public void readMark(MessageReadedContent messageReadedContent) {
        conversationService.messageMarkRead(messageReadedContent);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReadedPack, messageReadedPack);
        messageProducer.sendToUser(messageReadedContent.getToId(), MessageCommand.MSG_READED_RECEIPT, messageReadedPack, messageReadedContent.getAppId());
        syncToSender(messageReadedPack, messageReadedContent, MessageCommand.MSG_READED_NOTIFY);
    }

    public void syncToSender(MessageReadedPack messageReadedPack, MessageReadedContent messageReadedContent, Command command) {
        messageProducer.sendToUserExceptClient(messageReadedContent.getFromId(), command, messageReadedPack, messageReadedContent);
    }

    public void groupReadMark(MessageReadedContent messageReadedContent) {
        conversationService.messageMarkRead(messageReadedContent);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);
        syncToSender(messageReadedPack, messageReadedContent, GroupEventCommand.MSG_GROUP_READED_NOTIFY);
        if (!messageReadedContent.getFromId().equals(messageReadedContent.getToId())) {
            messageProducer.sendToUser(messageReadedPack.getToId(), GroupEventCommand.MSG_GROUP_READED_RECEIPT
                    , messageReadedContent, messageReadedContent.getAppId());
        }
    }

    public ResponseVO<SyncResp<OfflineMessageContent>> syncOfflineMessage(SyncReq req) {
        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key = req.getAppId() + ":" + Constants.RedisConstants.OFFLINEMESSAGE + ":" + req.getOperator();
        //获取最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if (!CollectionUtils.isEmpty(set)) {
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = Objects.requireNonNull(o.getScore()).longValue();
        }

        List<OfflineMessageContent> respList = new ArrayList<>();
        resp.setMaxSequence(maxSeq);

        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key,
                req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        if (!CollectionUtils.isEmpty(respList)) {
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }

    public void recallMessage(RecallMessageContent messageContent) {
        Long messageTime = messageContent.getMessageTime();
        Long now = System.currentTimeMillis();

        RecallMessageNotifyPack pack = new RecallMessageNotifyPack();
        BeanUtils.copyProperties(messageContent, pack);

        if (120000L < now - messageTime) {
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGE_RECALL_TIME_OUT), messageContent);
            return;
        }

        QueryWrapper<MessageBodyEntity> query = new QueryWrapper<>();
        query.eq("app_id", messageContent.getAppId());
        query.eq("message_key", messageContent.getMessageKey());
        MessageBodyEntity body = messageBodyMapper.selectOne(query);
        if (body == null) {
            //TODO ack失败 不存在的消息不能撤回
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGEBODY_IS_NOT_EXIST), messageContent);
            return;
        }

        if (body.getDelFlag() == DelFlag.DELETE.getCode()) {
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGE_IS_RECALLED), messageContent);
            return;
        }

        body.setDelFlag(DelFlag.DELETE.getCode());
        messageBodyMapper.update(body, query);

        if (messageContent.getConversationType() == ConversationType.P2P.getCode()) {
            // 找到fromId的队列
            String fromKey = messageContent.getAppId() + ":" + Constants.RedisConstants.OFFLINEMESSAGE + ":" + messageContent.getFromId();
            // 找到toId的队列
            String toKey = messageContent.getAppId() + ":" + Constants.RedisConstants.OFFLINEMESSAGE + ":" + messageContent.getToId();

            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent, offlineMessageContent);
            offlineMessageContent.setDelFlag(DelFlag.DELETE.getCode());
            offlineMessageContent.setMessageKey(messageContent.getMessageKey());
            offlineMessageContent.setConversationType(ConversationType.P2P.getCode());
            offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                    , messageContent.getFromId(), messageContent.getToId()));
            offlineMessageContent.setMessageBody(body.getMessageBody());

            long sequence = redisSequence.getSequence(messageContent.getAppId() + ":" + Constants.SequenceConstants.MESSAGE + ":" + ConversationIdGenerate.generateP2PId(messageContent.getFromId(), messageContent.getToId()));
            offlineMessageContent.setMessageSequence(sequence);

            long messageKey = SnowflakeIdWorker.nextId();

            redisTemplate.opsForZSet().add(fromKey, JSONObject.toJSONString(offlineMessageContent), messageKey);
            redisTemplate.opsForZSet().add(toKey, JSONObject.toJSONString(offlineMessageContent), messageKey);

            //ack
            recallAck(pack, ResponseVO.successResponse(), messageContent);
            //分发给同步端
            messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                    MessageCommand.MSG_RECALL_NOTIFY, pack, messageContent);
            //分发给对方
            messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_RECALL_NOTIFY,
                    pack, messageContent.getAppId());
        } else {

            GetGroupMemberIdReq getGroupMemberIdReq = new GetGroupMemberIdReq();
            getGroupMemberIdReq.setGroupId(messageContent.getToId());
            getGroupMemberIdReq.setAppId(messageContent.getAppId());

            List<String> groupMemberId = groupMemberService.getGroupMemberId(getGroupMemberIdReq);
            long sequence = redisSequence.getSequence(messageContent.getAppId() + ":" + Constants.SequenceConstants.MESSAGE + ":" + ConversationIdGenerate.generateP2PId(messageContent.getFromId(), messageContent.getToId()));

            //ack
            recallAck(pack, ResponseVO.successResponse(), messageContent);
            //发送给同步端
            messageProducer.sendToUserExceptClient(messageContent.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack
                    , messageContent);
            for (String memberId : groupMemberId) {
                String toKey = messageContent.getAppId() + ":" + Constants.SequenceConstants.MESSAGE + ":" + memberId;
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                offlineMessageContent.setDelFlag(DelFlag.DELETE.getCode());
                BeanUtils.copyProperties(messageContent, offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationType.GROUP.getCode());
                offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                        , messageContent.getFromId(), messageContent.getToId()));
                offlineMessageContent.setMessageBody(body.getMessageBody());
                offlineMessageContent.setMessageSequence(sequence);
                redisTemplate.opsForZSet().add(toKey, JSONObject.toJSONString(offlineMessageContent), sequence);

                groupMessageProducer.producer(messageContent.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack, messageContent);
            }
        }
    }

    private void recallAck(RecallMessageNotifyPack recallPack, ResponseVO<Object> success, ClientInfo clientInfo) {
        ResponseVO<Object> wrappedResp = success;
        messageProducer.sendToUser(recallPack.getFromId(),
                MessageCommand.MSG_RECALL_ACK, wrappedResp, clientInfo);
    }
}
