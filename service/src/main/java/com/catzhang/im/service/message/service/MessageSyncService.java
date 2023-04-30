package com.catzhang.im.service.message.service;

import com.catzhang.im.codec.pack.message.MessageReadedPack;
import com.catzhang.im.common.enums.command.Command;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.enums.command.MessageCommand;
import com.catzhang.im.common.model.message.MessageReadedContent;
import com.catzhang.im.common.model.message.MessageReceiveAckContent;
import com.catzhang.im.service.conversation.service.ConversationService;
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

    @Autowired
    ConversationService conversationService;

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
}
