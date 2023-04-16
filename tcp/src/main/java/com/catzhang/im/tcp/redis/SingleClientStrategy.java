package com.catzhang.im.tcp.redis;

import com.catzhang.im.codec.config.BootstrapConfig;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

/**
 * @author crazycatzhang
 */
public class SingleClientStrategy {

    public RedissonClient getRedisClient(BootstrapConfig.RedisConfig redisConfig) {
        Config config = new Config();
        String address = redisConfig.getSingle().getAddress();
        address = address.startsWith("redis://") ? address : "redis://" + address;
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisConfig.getDatabase())
                .setTimeout(redisConfig.getTimeout())
                .setConnectionMinimumIdleSize(redisConfig.getPoolMinIdle())
                .setConnectTimeout(redisConfig.getPoolConnTimeout())
                .setConnectionPoolSize(redisConfig.getPoolSize());
        if (StringUtils.isNotBlank(redisConfig.getPassword())) {
            serverConfig.setPassword(redisConfig.getPassword());
        }
        StringCodec stringCodec = new StringCodec();
        config.setCodec(stringCodec);
        return Redisson.create(config);
    }

}
