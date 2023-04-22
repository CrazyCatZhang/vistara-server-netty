package com.catzhang.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ConnectStatusEnum;
import com.catzhang.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author crazycatzhang
 */
@Component
public class UserSessionUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //获取用户的所有session
    public List<UserSession> getUserSession(Integer appId, String userId) {

        String userSessionKey = appId + Constants.RedisConstants.USER_SESSION_CONSTANTS
                + userId;
        Map<Object, Object> entries =
                stringRedisTemplate.opsForHash().entries(userSessionKey);
        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o : values) {
            String str = (String) o;
            UserSession session =
                    JSONObject.parseObject(str, UserSession.class);
            if (session.getConnectState().equals(ConnectStatusEnum.ONLINE_STATUS.getCode())) {
                list.add(session);
            }
        }
        return list;
    }

    //获取用户指定设备的session
    public UserSession getUserSession(Integer appId, String userId
            , Integer clientType, String imei) {

        String userSessionKey = appId + Constants.RedisConstants.USER_SESSION_CONSTANTS
                + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        assert o != null;
        return JSONObject.parseObject(o.toString(), UserSession.class);
    }

}
