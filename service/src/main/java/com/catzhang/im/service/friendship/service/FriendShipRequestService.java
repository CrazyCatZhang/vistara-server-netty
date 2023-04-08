package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.AddFriendShipRequestReq;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipRequestResp;

/**
 * @author crazycatzhang
 */
public interface FriendShipRequestService {
    ResponseVO<AddFriendShipRequestResp> addFriendShipRequest(AddFriendShipRequestReq req);
}
