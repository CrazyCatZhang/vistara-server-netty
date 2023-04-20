package com.catzhang.im.tcp.consume;

import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author crazycatzhang
 */
@Slf4j
@AllArgsConstructor
public class MessageConsumer {

    private static Integer brokerId;

    private static void consumeMessage() {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MESSAGESERVICETOIM + brokerId);
            channel.queueDeclare(Constants.RabbitConstants.MESSAGESERVICETOIM + brokerId, true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MESSAGESERVICETOIM + brokerId, Constants.RabbitConstants.MESSAGESERVICETOIM, brokerId.toString());
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

    public static void init(Integer brokerId) {
        if (StringUtils.isBlank(String.valueOf(MessageConsumer.brokerId))) {
            MessageConsumer.brokerId = brokerId;
        }
        consumeMessage();
    }
}
