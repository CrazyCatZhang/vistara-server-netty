package com.catzhang.im.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.codec.pack.user.UserCustomStatusChangeNotifyPack;
import com.catzhang.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.command.Command;
import com.catzhang.im.common.enums.command.UserEventCommand;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.common.model.UserSession;
import com.catzhang.im.service.friendship.service.FriendShipService;
import com.catzhang.im.service.user.model.UserStatusChangeNotifyContent;
import com.catzhang.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.catzhang.im.service.user.model.req.PullUserOnlineStatusReq;
import com.catzhang.im.service.user.model.req.SetUserCustomerStatusReq;
import com.catzhang.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.catzhang.im.service.user.model.resp.UserOnlineStatusResp;
import com.catzhang.im.service.user.service.UserStatusService;
import com.catzhang.im.service.utils.MessageProducer;
import com.catzhang.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author crazycatzhang
 */
@Service
public class UserStatusServiceImpl implements UserStatusService {

    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    FriendShipService friendShipService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void processUserOnlineStatusChangeNotify(UserStatusChangeNotifyContent content) {
        List<UserSession> userSession = userSessionUtils.getUserSession(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content, userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(userSession);

        syncSender(userStatusChangeNotifyPack, content.getUserId(),
                content);

        dispatcher(userStatusChangeNotifyPack, content.getUserId(),
                content.getAppId(), UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY);
    }

    private void syncSender(Object pack, String userId, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(userId,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC,
                pack, clientInfo);
    }

    private void dispatcher(Object pack, String userId, Integer appId, Command command) {
        List<String> allFriendId = friendShipService.getAllFriendId(userId, appId);
        for (String fid : allFriendId) {
            messageProducer.sendToUser(fid, command,
                    pack, appId);
        }

        String userKey = appId + ":" + Constants.RedisConstants.SUBSCRIBE + ":" + userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key : keys) {
            String filed = (String) key;
            Long expire = Long.valueOf((String) Objects.requireNonNull(stringRedisTemplate.opsForHash().get(userKey, filed)));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, command,
                        pack, appId);
            } else {
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }
    }

    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {
        Long subExpireTime = 0L;
        if (req != null && req.getSubTime() > 0) {
            subExpireTime = System.currentTimeMillis() + req.getSubTime();
        }

        assert req != null;
        for (String beSubUserId : req.getSubUserId()) {
            String userKey = req.getAppId() + ":" + Constants.RedisConstants.SUBSCRIBE + ":" + beSubUserId;
            stringRedisTemplate.opsForHash().put(userKey, req.getOperator(), subExpireTime.toString());
        }
    }

    @Override
    public void setUserCustomerStatus(SetUserCustomerStatusReq req) {
        UserCustomStatusChangeNotifyPack userCustomStatusChangeNotifyPack = new UserCustomStatusChangeNotifyPack();
        userCustomStatusChangeNotifyPack.setCustomStatus(req.getCustomStatus());
        userCustomStatusChangeNotifyPack.setCustomText(req.getCustomText());
        userCustomStatusChangeNotifyPack.setUserId(req.getUserId());
        stringRedisTemplate.opsForValue().set(req.getAppId()
                        + ":" + Constants.RedisConstants.USERCUSTOMERSTATUS + ":" + req.getUserId()
                , JSONObject.toJSONString(userCustomStatusChangeNotifyPack));

        syncSender(userCustomStatusChangeNotifyPack,
                req.getUserId(), new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        dispatcher(userCustomStatusChangeNotifyPack, req.getUserId(), req.getAppId(), UserEventCommand.USER_CUSTOM_STATUS_CHANGE_NOTIFY);
    }

    @Override
    public ResponseVO<Map<String, UserOnlineStatusResp>> queryFriendOnlineStatus(PullFriendOnlineStatusReq req) {
        List<String> allFriendId = friendShipService.getAllFriendId(req.getOperator(), req.getAppId());
        return ResponseVO.successResponse(getUserOnlineStatus(allFriendId, req.getAppId()));
    }

    @Override
    public ResponseVO<Map<String, UserOnlineStatusResp>> queryUserOnlineStatus(PullUserOnlineStatusReq req) {
        return ResponseVO.successResponse(getUserOnlineStatus(req.getUserList(), req.getAppId()));
    }

    private Map<String, UserOnlineStatusResp> getUserOnlineStatus(List<String> userId, Integer appId) {

        Map<String, UserOnlineStatusResp> result = new HashMap<>(userId.size());
        for (String uid : userId) {
            UserOnlineStatusResp resp = new UserOnlineStatusResp();
            List<UserSession> userSession = userSessionUtils.getUserSession(appId, uid);
            resp.setSession(userSession);
            String userKey = appId + ":" + Constants.RedisConstants.USERCUSTOMERSTATUS + ":" + uid;
            String s = stringRedisTemplate.opsForValue().get(userKey);
            if (StringUtils.isNotBlank(s)) {
                JSONObject parse = (JSONObject) JSON.parse(s);
                resp.setCustomText(parse.getString("customText"));
                resp.setCustomStatus(parse.getInteger("customStatus"));
            }
            result.put(uid, resp);
        }
        return result;
    }
}
