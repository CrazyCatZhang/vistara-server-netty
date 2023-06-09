package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.AddFriendShipRequestReq;
import com.catzhang.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.catzhang.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.catzhang.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipRequestResp;
import com.catzhang.im.service.friendship.model.resp.ApproveFriendRequestResp;
import com.catzhang.im.service.friendship.model.resp.GetFriendShipRequestResp;
import com.catzhang.im.service.friendship.model.resp.ReadFriendShipRequestResp;

/**
 * @author crazycatzhang
 */
public interface FriendShipRequestService {
    ResponseVO<AddFriendShipRequestResp> addFriendShipRequest(AddFriendShipRequestReq req);

    ResponseVO<ApproveFriendRequestResp> approveFriendRequest(ApproveFriendRequestReq req);

    ResponseVO<ReadFriendShipRequestResp> readFriendShipRequest(ReadFriendShipRequestReq req);

    ResponseVO<GetFriendShipRequestResp> getFriendShipRequest(GetFriendShipRequestReq req);
}
