package com.catzhang.im.service.friendship.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("add")
    public ResponseVO<AddFriendShipResp> addFriendShip(@RequestBody @Validated AddFriendShipReq req) {
        return friendShipService.addFriendShip(req);
    }

    @PutMapping("update")
    public ResponseVO<UpdateFriendShipResp> updateFriendShip(@RequestBody @Validated UpdateFriendShipReq req) {
        return friendShipService.updateFriendShip(req);
    }

    @DeleteMapping("delete")
    public ResponseVO<DeleteFriendShipResp> deleteFriendShip(@RequestBody @Validated DeleteFriendShipReq req) {
        return friendShipService.deleteFriendShip(req);
    }

    @DeleteMapping("deleteAll")
    public ResponseVO<DeleteAllFriendShipResp> deleteAllFriendShip(@RequestBody @Validated DeleteAllFriendShipReq req) {
        return friendShipService.deleteAllFriendShip(req);
    }

    @GetMapping("getAll")
    public ResponseVO<GetAllFriendShipResp> getAllFriendShip(@RequestBody @Validated GetAllFriendShipReq req) {
        return friendShipService.getAllFriendShip(req);
    }

    @GetMapping("getRelation")
    public ResponseVO<GetRelationResp> getRealation(@RequestBody @Validated GetRelationReq req) {
        return friendShipService.getRelation(req);
    }
}
