package com.catzhang.im.tcp.consume;

import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author crazycatzhang
 */
@Slf4j
public class MessageConsumer {

    private static void consumeMessage() {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MESSAGESERVICETOIM);
            channel.queueDeclare(Constants.RabbitConstants.MESSAGESERVICETOIM, true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MESSAGESERVICETOIM, Constants.RabbitConstants.MESSAGESERVICETOIM, "");
            channel.basicConsume(Constants.RabbitConstants.MESSAGESERVICETOIM, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String s = new String(body);
                    log.info(s);
                }
            });
        } catch (Exception e) {

        }
    }

    public static void init() {
        consumeMessage();
    }

}
