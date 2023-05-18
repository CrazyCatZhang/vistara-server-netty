package com.catzhang.im.service.media.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.command.Command;
import com.catzhang.im.common.enums.command.MediaEventCommand;
import com.catzhang.im.common.model.message.MediaMessageContent;
import com.catzhang.im.service.message.mq.ChatOperateReceiver;
import com.catzhang.im.service.utils.MessageProducer;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author crazycatzhang
 */
@Component
public class MediaOperateReceiver {

    @Autowired
    MessageProducer messageProducer;

    private void dispatchMessage(MediaMessageContent messageContent, Command command) {
        messageProducer.sendToUser(messageContent.getToId(), command, messageContent, messageContent.getAppId());
    }

    private void syncToSender(MediaMessageContent messageContent, Command command) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(), command, messageContent, messageContent);
    }

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.IMTOMEDIASERVICE, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.IMTOMEDIASERVICE)
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws IOException {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("CHAT MSG FORM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            MediaMessageContent messageContent = jsonObject.toJavaObject(MediaMessageContent.class);
            Command newCommand = null;
            if (command.equals(MediaEventCommand.CALL_VIDEO.getCommand())) {
                newCommand = MediaEventCommand.CALL_VIDEO;
            } else if (command.equals(MediaEventCommand.CALL_VOICE.getCommand())) {
                newCommand = MediaEventCommand.CALL_VOICE;
            } else if (command.equals(MediaEventCommand.ACCEPT_CALL.getCommand())) {
                newCommand = MediaEventCommand.ACCEPT_CALL;
            } else if (command.equals(MediaEventCommand.REJECT_CALL.getCommand())) {
                newCommand = MediaEventCommand.REJECT_CALL;
            } else if (command.equals(MediaEventCommand.HANG_UP.getCommand())) {
                newCommand = MediaEventCommand.HANG_UP;
            } else if (command.equals(MediaEventCommand.CANCEL_CALL.getCommand())) {
                newCommand = MediaEventCommand.CANCEL_CALL;
            } else if (command.equals(MediaEventCommand.TRANSMIT_OFFER.getCommand())) {
                newCommand = MediaEventCommand.TRANSMIT_OFFER;
            } else if (command.equals(MediaEventCommand.TRANSMIT_ANSWER.getCommand())) {
                newCommand = MediaEventCommand.TRANSMIT_ANSWER;
            } else if (command.equals(MediaEventCommand.TRANSMIT_ICE.getCommand())) {
                newCommand = MediaEventCommand.TRANSMIT_ICE;
            }
            dispatchMessage(messageContent, newCommand);
            syncToSender(messageContent, newCommand);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error("处理消息出现异常：{}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }
    }

}
