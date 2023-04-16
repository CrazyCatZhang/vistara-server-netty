package com.catzhang.im.tcp.redis;

import com.catzhang.im.codec.config.BootstrapConfig;
import org.redisson.api.RedissonClient;

/**
 * @author crazycatzhang
 */
public class RedisManager {

    private static RedissonClient redissonClient;

    public static void init(BootstrapConfig.RedisConfig redisConfig) {
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedisClient(redisConfig);
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
