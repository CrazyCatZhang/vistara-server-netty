package com.catzhang.im.service.user.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.model.UserStatusChangeNotifyContent;
import com.catzhang.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.catzhang.im.service.user.model.req.PullUserOnlineStatusReq;
import com.catzhang.im.service.user.model.req.SetUserCustomerStatusReq;
import com.catzhang.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.catzhang.im.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;

/**
 * @author crazycatzhang
 */
public interface UserStatusService {

    void processUserOnlineStatusChangeNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomerStatus(SetUserCustomerStatusReq req);

    ResponseVO<Map<String, UserOnlineStatusResp>> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    ResponseVO<Map<String, UserOnlineStatusResp>> queryUserOnlineStatus(PullUserOnlineStatusReq req);
}
