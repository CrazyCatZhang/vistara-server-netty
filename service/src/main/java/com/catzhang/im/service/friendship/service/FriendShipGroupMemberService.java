package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.catzhang.im.service.friendship.model.req.ClearFriendShipGroupMemberReq;
import com.catzhang.im.service.friendship.model.req.HandleAddFriendShipGroupMemberReq;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipGroupMemberResp;
import com.catzhang.im.service.friendship.model.resp.ClearFriendShipGroupMemberResp;
import com.catzhang.im.service.friendship.model.resp.HandleAddFriendShipGroupMemberResp;

/**
 * @author crazycatzhang
 */
public interface FriendShipGroupMemberService {

    ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMember(AddFriendShipGroupMemberReq req);

    ResponseVO<HandleAddFriendShipGroupMemberResp> handleAddFriendShipGroupMember(HandleAddFriendShipGroupMemberReq req);

    ResponseVO<ClearFriendShipGroupMemberResp> clearFriendShipGroupMember(ClearFriendShipGroupMemberReq req);
}
