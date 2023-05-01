package com.catzhang.im.service.user.service;

import com.catzhang.im.service.user.model.UserStatusChangeNotifyContent;
import com.catzhang.im.service.user.model.req.SubscribeUserOnlineStatusReq;

/**
 * @author crazycatzhang
 */
public interface UserStatusService {

    void processUserOnlineStatusChangeNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);
}
