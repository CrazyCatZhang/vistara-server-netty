package com.catzhang.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.codec.proto.Message;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author crazycatzhang
 */
public class MessageProducer {

    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);


    public static void sendMessage(Message message, Integer command) {
        Channel channel = null;
        String channelName = Constants.RabbitConstants.IMTOMESSAGESERVICE;
        try {
            channel = MqFactory.getChannel(channelName);
            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command", command);
            o.put("clientType", message.getMessageHeader().getClientType());
            o.put("imei", message.getMessageHeader().getImei());
            o.put("appId", message.getMessageHeader().getAppId());
            channel.basicPublish(channelName, "",
                    null, o.toJSONString().getBytes());
        } catch (Exception e) {
            logger.error("Failed to publish message {}", e.getMessage());
        }
    }

}
