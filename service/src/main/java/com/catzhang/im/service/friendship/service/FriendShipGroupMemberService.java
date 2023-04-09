package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;

/**
 * @author crazycatzhang
 */
public interface FriendShipGroupMemberService {

    ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMember(AddFriendShipGroupMemberReq req);

    ResponseVO<HandleAddFriendShipGroupMemberResp> handleAddFriendShipGroupMember(HandleAddFriendShipGroupMemberReq req);

    ResponseVO<ClearFriendShipGroupMemberResp> clearFriendShipGroupMember(ClearFriendShipGroupMemberReq req);

    ResponseVO<DeleteFriendShipGroupMemberResp> deleteFriendShipGroupMember(DeleteFriendShipGroupMemberReq req);

    ResponseVO<HandleDeleteFriendShipGroupMemberResp> handleDeleteFriendShipGroupMember(HandleDeleteFriendShipGroupMemberReq req);

    ResponseVO<GetAllFriendShipGroupMemberResp> getAllFriendShipGroupMember(GetAllFriendShipGroupMemberReq req);
}
