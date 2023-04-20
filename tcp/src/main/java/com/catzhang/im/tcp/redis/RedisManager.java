package com.catzhang.im.tcp.redis;

import com.catzhang.im.codec.config.BootstrapConfig;
import com.catzhang.im.tcp.listener.UserLoginMessageListener;
import org.redisson.api.RedissonClient;

/**
 * @author crazycatzhang
 */
public class RedisManager {

    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config) {
        loginModel = config.getLim().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedisClient(config.getLim().getRedis());
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
