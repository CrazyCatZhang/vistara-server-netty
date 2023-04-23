package com.catzhang.im.tcp.consume;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.codec.proto.MessagePack;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.tcp.consume.process.BaseProcess;
import com.catzhang.im.tcp.consume.process.ProcessFactory;
import com.catzhang.im.tcp.utils.MqFactory;
import com.rabbitmq.client.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author crazycatzhang
 */
@AllArgsConstructor
public class MessageConsumer {

    private static Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private static String brokerId;

    private static void consumeMessage() {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MESSAGESERVICETOIM + brokerId);
            channel.queueDeclare(Constants.RabbitConstants.MESSAGESERVICETOIM + brokerId, true, false, false, null);
            channel.exchangeDeclare(Constants.RabbitConstants.MESSAGESERVICETOIM, BuiltinExchangeType.DIRECT, true);
            channel.queueBind(Constants.RabbitConstants.MESSAGESERVICETOIM + brokerId, Constants.RabbitConstants.MESSAGESERVICETOIM, brokerId);
            channel.basicConsume(Constants.RabbitConstants.MESSAGESERVICETOIM + brokerId, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    try {
                        String msgStr = new String(body);
                        logger.info(msgStr);
                        MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);
                        BaseProcess messageProcess = ProcessFactory.getMessageProcess(messagePack.getCommand());
                        messageProcess.process(messagePack);

                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        channel.basicNack(envelope.getDeliveryTag(), false, false);
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    public static void init() {
        consumeMessage();
    }

    public static void init(Integer brokerId) {
        if (StringUtils.isBlank(MessageConsumer.brokerId)) {
            MessageConsumer.brokerId = String.valueOf(brokerId);
        }
        consumeMessage();
    }
}
