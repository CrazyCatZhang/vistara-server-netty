package com.catzhang.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.tcp.consume.MessageConsumer;
import com.catzhang.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author crazycatzhang
 */
public class MessageProducer {

    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);


    public static void sendMessage(Object message) {
        Channel channel = null;
        String channelName = "";
        try {
            channel = MqFactory.getChannel(channelName);
            channel.basicPublish(channelName, "", null, JSONObject.toJSONString(message).getBytes());
        } catch (Exception e) {
            logger.error("Failed to publish message {}", e.getMessage());
        }
    }

}
