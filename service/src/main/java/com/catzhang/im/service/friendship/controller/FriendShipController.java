package com.catzhang.im.service.friendship.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.ImportFriendShipReq;
import com.catzhang.im.service.friendship.model.resp.ImportFriendShipResp;
import com.catzhang.im.service.friendship.service.FriendShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/friendship")
public class FriendShipController {

    @Autowired
    FriendShipService friendShipService;

    @PostMapping("import")
    public ResponseVO<ImportFriendShipResp> importFriendShip(@RequestBody @Validated ImportFriendShipReq req) {
        return friendShipService.importFriendShip(req);
    }
}
