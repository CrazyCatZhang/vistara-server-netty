package com.catzhang.im.service.message.service;

import com.catzhang.im.common.enums.command.MessageCommand;
import com.catzhang.im.common.model.message.MessageReceiveAckContent;
import com.catzhang.im.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author crazycatzhang
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {
        messageProducer.sendToUser(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK, messageReceiveAckContent, messageReceiveAckContent.getAppId());
    }

}
