package com.catzhang.im.service.friendship.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.ImportFriendShipReq;
import com.catzhang.im.service.friendship.model.resp.ImportFriendShipResp;

/**
 * @author crazycatzhang
 */
public interface FriendShipService {
    ResponseVO<ImportFriendShipResp> importFriendShip(ImportFriendShipReq req);
}
