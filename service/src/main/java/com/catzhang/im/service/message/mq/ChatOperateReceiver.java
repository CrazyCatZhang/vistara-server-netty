package com.catzhang.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.command.MessageCommand;
import com.catzhang.im.common.model.message.MessageContent;
import com.catzhang.im.common.model.message.MessageReadedContent;
import com.catzhang.im.common.model.message.MessageReceiveAckContent;
import com.catzhang.im.common.model.message.RecallMessageContent;
import com.catzhang.im.service.message.service.MessageSyncService;
import com.catzhang.im.service.message.service.P2PMessageService;
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
import java.util.Objects;

/**
 * @author crazycatzhang
 */
@Component
public class ChatOperateReceiver {

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.IMTOMESSAGESERVICE, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.IMTOMESSAGESERVICE)
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

            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                p2PMessageService.process(messageContent);
            } else if (command.equals(MessageCommand.MSG_RECEIVE_ACK.getCommand())) {
                MessageReceiveAckContent messageContent = jsonObject.toJavaObject(MessageReceiveAckContent.class);
                messageSyncService.receiveMark(messageContent);
            } else if (command.equals(MessageCommand.MSG_READED.getCommand())) {
                MessageReadedContent messageReadedContent = jsonObject.toJavaObject(MessageReadedContent.class);
                messageSyncService.readMark(messageReadedContent);
            } else if (Objects.equals(command, MessageCommand.MSG_RECALL.getCommand())) {
                //  撤回消息
                RecallMessageContent messageContent = JSON.parseObject(msg, new TypeReference<RecallMessageContent>() {
                }.getType());
                messageSyncService.recallMessage(messageContent);
            }

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
