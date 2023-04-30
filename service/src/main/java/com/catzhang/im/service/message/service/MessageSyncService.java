package com.catzhang.im.service.message.service;

import com.catzhang.im.codec.pack.message.MessageReadedPack;
import com.catzhang.im.common.enums.command.MessageCommand;
import com.catzhang.im.common.model.message.MessageReadedContent;
import com.catzhang.im.common.model.message.MessageReceiveAckContent;
import com.catzhang.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
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

    public void readMark(MessageReadedContent messageReadedContent) {
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReadedPack, messageReadedPack);
        messageProducer.sendToUser(messageReadedContent.getToId(), MessageCommand.MSG_READED_RECEIPT, messageReadedPack, messageReadedContent.getAppId());
        syncToSender(messageReadedPack, messageReadedContent);
    }

    public void syncToSender(MessageReadedPack messageReadedPack, MessageReadedContent messageReadedContent) {
        messageProducer.sendToUserExceptClient(messageReadedContent.getFromId(), MessageCommand.MSG_READED_NOTIFY, messageReadedPack, messageReadedContent);
    }
}
