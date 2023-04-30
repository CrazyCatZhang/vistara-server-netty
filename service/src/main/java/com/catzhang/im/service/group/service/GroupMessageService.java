package com.catzhang.im.service.group.service;

import com.catzhang.im.codec.pack.message.ChatMessageAck;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.model.message.GroupMessageContent;
import com.catzhang.im.service.group.model.req.GetGroupMemberIdReq;
import com.catzhang.im.service.group.model.req.SendGroupMessageReq;
import com.catzhang.im.service.group.model.resp.SendGroupMessageResp;
import com.catzhang.im.service.message.service.MessageStoreService;
import com.catzhang.im.service.message.service.VerifySendMessageService;
import com.catzhang.im.service.utils.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author crazycatzhang
 */
@Service
public class GroupMessageService {

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    VerifySendMessageService verifySendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    private static Logger logger = LoggerFactory.getLogger(GroupMessageService.class);

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-group-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    public void process(GroupMessageContent messageContent) {

        logger.info("消息开始处理：{}", messageContent.getMessageId());
        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();
        ResponseVO responseVO = verifyImServerPermission(fromId, groupId, appId);
        if (responseVO.isOk()) {
            threadPoolExecutor.execute(() -> {
                messageStoreService.storeGroupMessage(messageContent);
                ack(messageContent, responseVO);
                syncToSender(messageContent);
                dispatchMessage(messageContent);
            });
        } else {
            ack(messageContent, responseVO);
        }

    }

    private void ack(GroupMessageContent messageContent, ResponseVO responseVO) {
        logger.info("msg ack,msgId={},checkResult{}", messageContent.getMessageId(), responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        messageProducer.sendToUser(messageContent.getFromId(), GroupEventCommand.GROUP_MSG_ACK, responseVO, messageContent);
    }

    private void syncToSender(GroupMessageContent messageContent) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(), GroupEventCommand.MSG_GROUP, messageContent, messageContent);
    }

    private void dispatchMessage(GroupMessageContent messageContent) {

        GetGroupMemberIdReq getGroupMemberIdReq = new GetGroupMemberIdReq();
        getGroupMemberIdReq.setGroupId(messageContent.getGroupId());
        getGroupMemberIdReq.setAppId(messageContent.getAppId());
        List<String> groupMemberId = groupMemberService.getGroupMemberId(getGroupMemberIdReq);

        for (String memberId : groupMemberId) {
            if (!memberId.equals(messageContent.getFromId())) {
                messageProducer.sendToUser(memberId,
                        GroupEventCommand.MSG_GROUP,
                        messageContent, messageContent.getAppId());
            }
        }
    }

    public ResponseVO verifyImServerPermission(String fromId, String groupId, Integer appId) {
        ResponseVO responseVO = verifySendMessageService.verifySendGroupMessage(fromId, groupId, appId);
        return responseVO;
    }

    public ResponseVO<SendGroupMessageResp> send(SendGroupMessageReq req) {
        SendGroupMessageResp sendGroupMessageResp = new SendGroupMessageResp();
        GroupMessageContent message = new GroupMessageContent();
        BeanUtils.copyProperties(req, message);

        messageStoreService.storeGroupMessage(message);

        sendGroupMessageResp.setMessageKey(message.getMessageKey());
        sendGroupMessageResp.setMessageTime(System.currentTimeMillis());
        //2.发消息给同步在线端
        syncToSender(message);
        //3.发消息给对方在线端
        dispatchMessage(message);

        return ResponseVO.successResponse(sendGroupMessageResp);
    }

}
