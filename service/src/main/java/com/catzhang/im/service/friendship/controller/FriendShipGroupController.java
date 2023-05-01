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

    @PostMapping("add")
    public ResponseVO<AddFriendShipGroupResp> addFriendShipGroup(@RequestBody @Validated AddFriendShipGroupReq req) {
        return friendShipGroupService.addFriendShipGroup(req);
    }

    @DeleteMapping("delete")
    public ResponseVO<DeleteFriendShipGroupResp> deleteFriendShipGroup(@RequestBody @Validated DeleteFriendShipGroupReq req) {
        return friendShipGroupService.deleteFriendShipGroup(req);
    }

    @GetMapping("getAll")
    public ResponseVO<GetAllFriendShipGroupResp> getAllFriendShipGroup(@RequestBody @Validated GetAllFriendShipGroupReq req) {
        return friendShipGroupService.getAllFriendShipGroup(req);
    }

    @PostMapping("/member/add")
    public ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMember(@RequestBody @Validated AddFriendShipGroupMemberReq req) {
        return friendShipGroupMemberService.addFriendShipGroupMember(req);
    }

    @DeleteMapping("/member/delete")
    public ResponseVO<DeleteFriendShipGroupMemberResp> deleteFriendShipGroupMember(@RequestBody @Validated DeleteFriendShipGroupMemberReq req) {
        return friendShipGroupMemberService.deleteFriendShipGroupMember(req);
    }

    //TODO: 同步最大分组Sequence
}
