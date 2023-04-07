package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;

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
}
