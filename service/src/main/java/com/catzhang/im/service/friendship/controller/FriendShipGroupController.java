package com.catzhang.im.service.friendship.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipGroupMemberService;
import com.catzhang.im.service.friendship.service.FriendShipGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/friendship/group")
public class FriendShipGroupController {

    @Autowired
    FriendShipGroupMemberService friendShipGroupMemberService;

    @Autowired
    FriendShipGroupService friendShipGroupService;

    @RequestMapping("add")
    public ResponseVO<AddFriendShipGroupResp> addFriendShipGroup(@RequestBody @Validated AddFriendShipGroupReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipGroupService.addFriendShipGroup(req);
    }

    @RequestMapping("delete")
    public ResponseVO<DeleteFriendShipGroupResp> deleteFriendShipGroup(@RequestBody @Validated DeleteFriendShipGroupReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipGroupService.deleteFriendShipGroup(req);
    }

    @RequestMapping("getAll")
    public ResponseVO<GetAllFriendShipGroupResp> getAllFriendShipGroup(@RequestBody @Validated GetAllFriendShipGroupReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipGroupService.getAllFriendShipGroup(req);
    }

    @RequestMapping("/member/add")
    public ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMember(@RequestBody @Validated AddFriendShipGroupMemberReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipGroupMemberService.addFriendShipGroupMember(req);
    }

    @RequestMapping("/member/delete")
    public ResponseVO<DeleteFriendShipGroupMemberResp> deleteFriendShipGroupMember(@RequestBody @Validated DeleteFriendShipGroupMemberReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipGroupMemberService.deleteFriendShipGroupMember(req);
    }

    //TODO: 同步最大分组Sequence
}
