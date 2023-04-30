package com.catzhang.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.message.dao.MessageBodyEntity;
import com.catzhang.message.model.HandleStoreGroupMessageDto;
import com.catzhang.message.model.HandleStoreP2PMessageDto;
import com.catzhang.message.service.StoreMessageService;
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
public class StoreGroupMessageReceiver {

    @Autowired
    StoreMessageService storeMessageService;

    private static Logger logger = LoggerFactory.getLogger(StoreGroupMessageReceiver.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.STOREGROUPMESSAGE, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.STOREGROUPMESSAGE)
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
            HandleStoreGroupMessageDto handleStoreGroupMessageDto = jsonObject.toJavaObject(HandleStoreGroupMessageDto.class);
            MessageBodyEntity messageBodyEntity = jsonObject.getObject("messageBody", MessageBodyEntity.class);
            handleStoreGroupMessageDto.setMessageBodyEntity(messageBodyEntity);
            storeMessageService.handleStoreGroupMessage(handleStoreGroupMessageDto);
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
