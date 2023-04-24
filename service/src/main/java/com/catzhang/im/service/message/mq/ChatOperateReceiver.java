package com.catzhang.im.service.message.mq;

import com.catzhang.im.common.constant.Constants;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author crazycatzhang
 */
@Component
public class ChatOperateReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);


    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.IMTOMESSAGESERVICE, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.IMTOMESSAGESERVICE)
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.info("CHAT MSG FORM QUEUE ::: {}", msg);

        try {

        } catch (Exception e) {

        }
    }

}
