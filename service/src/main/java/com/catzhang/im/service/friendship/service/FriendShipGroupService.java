package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.req.GetFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipGroupResp;
import com.catzhang.im.service.friendship.model.resp.GetFriendShipGroupResp;

/**
 * @author crazycatzhang
 */
public interface FriendShipGroupService {

    ResponseVO<AddFriendShipGroupResp> addFriendShipGroup(AddFriendShipGroupReq req);

    ResponseVO<GetFriendShipGroupResp> getFriendShipGroup(GetFriendShipGroupReq req);
}
