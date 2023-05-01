package com.catzhang.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.catzhang.im.codec.pack.message.MessageReadedPack;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.command.Command;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.enums.command.MessageCommand;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
import com.catzhang.im.common.model.message.MessageReadedContent;
import com.catzhang.im.common.model.message.MessageReceiveAckContent;
import com.catzhang.im.common.model.message.OfflineMessageContent;
import com.catzhang.im.service.conversation.service.ConversationService;
import com.catzhang.im.service.utils.MessageProducer;
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
}
