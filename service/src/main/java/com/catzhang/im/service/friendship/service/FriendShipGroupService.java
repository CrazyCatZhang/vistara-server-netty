package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.req.GetAllFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.req.GetFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipGroupResp;
import com.catzhang.im.service.friendship.model.resp.DeleteFriendShipGroupResp;
import com.catzhang.im.service.friendship.model.resp.GetAllFriendShipGroupResp;
import com.catzhang.im.service.friendship.model.resp.GetFriendShipGroupResp;

/**
 * @author crazycatzhang
 */
public interface FriendShipGroupService {

    ResponseVO<AddFriendShipGroupResp> addFriendShipGroup(AddFriendShipGroupReq req);

    ResponseVO<GetFriendShipGroupResp> getFriendShipGroup(GetFriendShipGroupReq req);

    ResponseVO<DeleteFriendShipGroupResp> deleteFriendShipGroup(DeleteFriendShipGroupReq req);

    ResponseVO<GetAllFriendShipGroupResp> getAllFriendShipGroup(GetAllFriendShipGroupReq req);
}
