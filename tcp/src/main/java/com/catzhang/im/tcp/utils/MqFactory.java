package com.catzhang.im.tcp.utils;

import com.catzhang.im.codec.config.BootstrapConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author crazycatzhang
 */
public class MqFactory {

    private static ConnectionFactory factory = null;

    private static Channel defaultChannel = null;

    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    public static void init(BootstrapConfig.Rabbitmq rabbitmqConfig) {
        if (factory == null) {
            factory = new ConnectionFactory();
            factory.setHost(rabbitmqConfig.getHost());
            factory.setPort(rabbitmqConfig.getPort());
            factory.setUsername(rabbitmqConfig.getUserName());
            factory.setPassword(rabbitmqConfig.getPassword());
            factory.setVirtualHost(rabbitmqConfig.getVirtualHost());
        }
    }

    private static Connection getConnection() throws IOException, TimeoutException {
        return factory.newConnection();
    }

    public static Channel getChannel(String channelName) throws IOException, TimeoutException {
        Channel channel = channelMap.get(channelName);
        if (channel == null) {
            channel = getConnection().createChannel();
            channelMap.put(channelName, channel);
        }
        return channel;
    }
}
