package com.catzhang.im.service.group.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.model.message.GroupMessageContent;
import com.catzhang.im.common.model.message.MessageReadedContent;
import com.catzhang.im.service.group.service.GroupMessageService;
import com.catzhang.im.service.message.service.MessageSyncService;
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
public class GroupChatOperateReceiver {

    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    private static Logger logger = LoggerFactory.getLogger(GroupChatOperateReceiver.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.IMTOGROUPSERVICE, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.IMTOGROUPSERVICE)
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

            if (command.equals(GroupEventCommand.MSG_GROUP.getCommand())) {
                GroupMessageContent groupMessageContent = jsonObject.toJavaObject(GroupMessageContent.class);
                groupMessageService.process(groupMessageContent);
            } else if (command.equals(GroupEventCommand.MSG_GROUP_READED.getCommand())) {
                MessageReadedContent messageReadedContent = JSON.parseObject(msg, new TypeReference<MessageReadedContent>() {
                }.getType());
                messageSyncService.groupReadMark(messageReadedContent);
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
