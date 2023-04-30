package com.catzhang.im.service.sequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


/**
 * @author crazycatzhang
 */
@Service
public class RedisSequence {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public long getSequence( String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

}
