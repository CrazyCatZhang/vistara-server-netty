package com.catzhang.im.tcp.utils;

import com.alibaba.fastjson.JSON;
import com.catzhang.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.catzhang.im.codec.proto.MessageHeader;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ConnectStatus;
import com.catzhang.im.common.enums.ImConnectStatus;
import com.catzhang.im.common.enums.command.UserEventCommand;
import com.catzhang.im.common.model.UserClientDto;
import com.catzhang.im.common.model.UserSession;
import com.catzhang.im.tcp.publish.MessageProducer;
import com.catzhang.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author crazycatzhang
 */
@Slf4j
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId, Integer clientType, String imei, NioSocketChannel channel) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setAppId(appId);
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        CHANNELS.put(userClientDto, channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        return CHANNELS.get(userClientDto);
    }

    public static void remove(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        CHANNELS.remove(userClientDto);
    }

    public static void remove(NioSocketChannel channel) {
        CHANNELS.entrySet().stream().filter(item -> item.getValue() == channel).forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

    public static void removeUserSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.APPID)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.CLIENTTYPE)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.IMEI)).get();

        SessionSocketHolder.remove(appId, userId, clientType, imei);

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.USER_SESSION_CONSTANTS + userId);
        map.remove(clientType + ":" + imei);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setClientType(clientType);

        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        userStatusChangeNotifyPack.setAppId(appId);
        userStatusChangeNotifyPack.setUserId(userId);
        userStatusChangeNotifyPack.setStatus(ConnectStatus.OFFLINE_STATUS.getCode());
        MessageProducer.sendMessage(userStatusChangeNotifyPack, messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

        nioSocketChannel.close();
    }

    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {
        log.info("用户已离线.....");
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.APPID)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.CLIENTTYPE)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.IMEI)).get();

        SessionSocketHolder.remove(appId, userId, clientType, imei);

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.USER_SESSION_CONSTANTS + userId);
        String session = map.get(clientType.toString() + ":" + imei);
        if (!StringUtils.isBlank(session)) {
            UserSession userSession = JSON.parseObject(session, UserSession.class);
            userSession.setConnectState(ImConnectStatus.OFFLINE_STATUS.getCode());
            map.put(clientType + ":" + imei, JSON.toJSONString(userSession));
        }

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setClientType(clientType);

        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        userStatusChangeNotifyPack.setAppId(appId);
        userStatusChangeNotifyPack.setUserId(userId);
        userStatusChangeNotifyPack.setStatus(ConnectStatus.OFFLINE_STATUS.getCode());
        MessageProducer.sendMessage(userStatusChangeNotifyPack, messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

        nioSocketChannel.close();
    }

    public static List<NioSocketChannel> get(Integer appId, String userId) {
        Set<UserClientDto> userClientDtos = CHANNELS.keySet();
        List<NioSocketChannel> channelList = new ArrayList<>();
        userClientDtos.forEach(userClientDto -> {
            if (userClientDto.getAppId().equals(appId) && userClientDto.getUserId().equals(userId)) {
                channelList.add(CHANNELS.get(userClientDto));
            }
        });

        return channelList;
    }
}
