package com.catzhang.im.tcp.utils;

import com.alibaba.fastjson.JSON;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ImConnectStatus;
import com.catzhang.im.common.model.UserClientDto;
import com.catzhang.im.common.model.UserSession;
import com.catzhang.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author crazycatzhang
 */
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId, Integer clientType, NioSocketChannel channel) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        CHANNELS.put(userClientDto, channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        return CHANNELS.get(userClientDto);
    }

    public static void remove(Integer appId, String userId, Integer clientType) {
        UserClientDto userClientDto = new UserClientDto();
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
        SessionSocketHolder.remove(appId, userId, clientType);

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.USER_SESSION_CONSTANTS + userId);
        map.remove(String.valueOf(clientType));
        nioSocketChannel.close();
    }

    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.APPID)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.CLIENTTYPE)).get();
        SessionSocketHolder.remove(appId, userId, clientType);

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.USER_SESSION_CONSTANTS + userId);
        String session = map.get(clientType.toString());
        if (!StringUtils.isBlank(session)) {
            UserSession userSession = JSON.parseObject(session, UserSession.class);
            userSession.setConnectState(ImConnectStatus.OFFLINE_STATUS.getCode());
            map.put(clientType.toString(), JSON.toJSONString(userSession));
        }

        nioSocketChannel.close();
    }
}
