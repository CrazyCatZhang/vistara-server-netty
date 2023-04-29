package com.catzhang.im.service.message.service;

import com.catzhang.im.codec.pack.message.ChatMessageAck;
import com.catzhang.im.codec.pack.message.MessageReceiveServerAckPack;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.command.MessageCommand;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.common.model.message.MessageContent;
import com.catzhang.im.service.message.model.req.SendMessageReq;
import com.catzhang.im.service.message.model.resp.SendMessageResp;
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
public class P2PMessageService {

    @Autowired
    VerifySendMessageService verifySendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);

    public void process(MessageContent messageContent) {

        logger.info("消息开始处理：{}", messageContent.getMessageId());
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
//        ResponseVO responseVO = verifyImServerPermission(fromId, toId, appId);
//        if (responseVO.isOk()) {
        threadPoolExecutor.execute(() -> {
            messageStoreService.storeP2PMessage(messageContent);
            ack(messageContent, ResponseVO.successResponse());
            syncToSender(messageContent);
            List<ClientInfo> clientInfos = dispatchMessage(messageContent);
            if (clientInfos.isEmpty()) {
                receiverAck(messageContent);
            }
        });
//        } else {
//            ack(messageContent, responseVO);
//        }

    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        logger.info("msg ack,msgId={},checkResult{}", messageContent.getMessageId(), responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK, responseVO, messageContent);
    }

    public void receiverAck(MessageContent messageContent) {
        MessageReceiveServerAckPack pack = new MessageReceiveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_RECEIVE_ACK,
                pack, new ClientInfo(messageContent.getAppId(), messageContent.getClientType()
                        , messageContent.getImei()));
    }

    private void syncToSender(MessageContent messageContent) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent, messageContent);
    }

    private List<ClientInfo> dispatchMessage(MessageContent messageContent) {
        List<ClientInfo> clientInfos = messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P, messageContent, messageContent.getAppId());
        return clientInfos;
    }

    public ResponseVO verifyImServerPermission(String fromId, String toId, Integer appId) {
        ResponseVO responseVO = verifySendMessageService.verifySenderForbiddenAndMuted(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }
        responseVO = verifySendMessageService.verifyFriendship(fromId, toId, appId);
        return responseVO;
    }

    public ResponseVO<SendMessageResp> send(SendMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req, message);
        //插入数据
        messageStoreService.storeP2PMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        //2.发消息给同步在线端
        syncToSender(message);
        //3.发消息给对方在线端
        dispatchMessage(message);
        return ResponseVO.successResponse(sendMessageResp);

    }
}
