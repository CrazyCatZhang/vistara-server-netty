package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.AddFriendShipReq;
import com.catzhang.im.service.friendship.model.req.HandleAddFriendShipReq;
import com.catzhang.im.service.friendship.model.req.ImportFriendShipReq;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipResp;
import com.catzhang.im.service.friendship.model.resp.HandleAddFriendShipResp;
import com.catzhang.im.service.friendship.model.resp.ImportFriendShipResp;

/**
 * @author crazycatzhang
 */
public interface FriendShipService {
    ResponseVO<ImportFriendShipResp> importFriendShip(ImportFriendShipReq req);

    ResponseVO<AddFriendShipResp> addFriendShip(AddFriendShipReq req);

    ResponseVO<HandleAddFriendShipResp> handleAddFriendShip(HandleAddFriendShipReq req);
}
