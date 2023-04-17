package com.catzhang.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author crazycatzhang
 */
@Slf4j
public class MessageProducer {

    public static void sendMessage(Object message) {
        Channel channel = null;
        String channelName = "";
        try {
            channel = MqFactory.getChannel(channelName);
            channel.basicPublish(channelName, "", null, JSONObject.toJSONString(message).getBytes());
        } catch (Exception e) {
            log.error("Failed to publish message {}", e.getMessage());
        }
    }

}
