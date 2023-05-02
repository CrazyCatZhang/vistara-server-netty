package com.catzhang.im.service.friendship.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
import com.catzhang.im.service.friendship.dao.FriendShipEntity;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/friendship")
public class FriendShipController {

    @Autowired
    FriendShipService friendShipService;

    @PostMapping("import")
    public ResponseVO<ImportFriendShipResp> importFriendShip(@RequestBody @Validated ImportFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.importFriendShip(req);
    }

    @PostMapping("add")
    public ResponseVO<AddFriendShipResp> addFriendShip(@RequestBody @Validated AddFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.addFriendShip(req);
    }

    @PutMapping("update")
    public ResponseVO<UpdateFriendShipResp> updateFriendShip(@RequestBody @Validated UpdateFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.updateFriendShip(req);
    }

    @DeleteMapping("delete")
    public ResponseVO<DeleteFriendShipResp> deleteFriendShip(@RequestBody @Validated DeleteFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.deleteFriendShip(req);
    }

    @DeleteMapping("deleteAll")
    public ResponseVO<DeleteAllFriendShipResp> deleteAllFriendShip(@RequestBody @Validated DeleteAllFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.deleteAllFriendShip(req);
    }

    @GetMapping("getAll")
    public ResponseVO<GetAllFriendShipResp> getAllFriendShip(@RequestBody @Validated GetAllFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.getAllFriendShip(req);
    }

    @GetMapping("getRelation")
    public ResponseVO<GetRelationResp> getRelation(@RequestBody @Validated GetRelationReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.getRelation(req);
    }

    @GetMapping("verify")
    public ResponseVO<List<VerifyFriendShipResp>> verifyFriendShip(@RequestBody @Validated VerifyFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.verifyFriendShip(req);
    }

    @PutMapping("black")
    public ResponseVO<AddFriendShipBlackResp> addFriendShipBlack(@RequestBody @Validated AddFriendShipBlackReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.blackFriendShip(req);
    }

    @PutMapping("deleteBlack")
    public ResponseVO<DeleteFriendShipBlackResp> deleteFriendShipBlack(@RequestBody @Validated DeleteFriendShipBlackReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.deleteFriendShipBlack(req);
    }

    @GetMapping("verifyBlack")
    public ResponseVO<List<VerifyFriendShipResp>> verifyFriendShipBlack(@RequestBody @Validated VerifyFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipService.verifyFriendShipBlack(req);
    }

    @GetMapping("syncFriendshipList")
    public ResponseVO<SyncResp<FriendShipEntity>> syncFriendshipList(@RequestBody @Validated SyncReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return friendShipService.syncFriendshipList(req);
    }
}
