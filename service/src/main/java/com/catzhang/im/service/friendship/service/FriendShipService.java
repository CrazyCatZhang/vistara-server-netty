package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;

import java.util.List;

/**
 * @author crazycatzhang
 */
public interface FriendShipService {
    ResponseVO<ImportFriendShipResp> importFriendShip(ImportFriendShipReq req);

    ResponseVO<AddFriendShipResp> addFriendShip(AddFriendShipReq req);

    ResponseVO<HandleAddFriendShipResp> handleAddFriendShip(HandleAddFriendShipReq req);

    ResponseVO<UpdateFriendShipResp> updateFriendShip(UpdateFriendShipReq req);

    ResponseVO<HandleUpdateFriendShipResp> handleUpdateFriendShip(HandleUpdateFriendShipReq req);

    ResponseVO<DeleteFriendShipResp> deleteFriendShip(DeleteFriendShipReq req);

    ResponseVO<DeleteAllFriendShipResp> deleteAllFriendShip(DeleteAllFriendShipReq req);

    ResponseVO<GetAllFriendShipResp> getAllFriendShip(GetAllFriendShipReq req);

    ResponseVO<GetRelationResp> getRelation(GetRelationReq req);

    ResponseVO<List<VerifyFriendShipResp>> verifyFriendShip(VerifyFriendShipReq req);

    ResponseVO<AddFriendShipBlackResp> blackFriendShip(AddFriendShipBlackReq req);
}
