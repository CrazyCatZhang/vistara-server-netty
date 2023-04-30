package com.catzhang.im.service.utils;

import com.catzhang.im.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author crazycatzhang
 */
@Service
public class WriteUserSequence {

    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSequence(Integer appId, String userId, String type, Long seq) {
        String key = appId + ":" + Constants.RedisConstants.SEQUENCEPREFIX + ":" + userId;
        redisTemplate.opsForHash().put(key, type, seq);
    }

}
