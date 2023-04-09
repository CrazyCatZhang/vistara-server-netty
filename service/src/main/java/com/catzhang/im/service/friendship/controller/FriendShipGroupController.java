package com.catzhang.im.service.friendship.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.catzhang.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipGroupMemberResp;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipGroupResp;
import com.catzhang.im.service.friendship.service.FriendShipGroupMemberService;
import com.catzhang.im.service.friendship.service.FriendShipGroupService;
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

    @PostMapping("/member/add")
    public ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMember(@RequestBody @Validated AddFriendShipGroupMemberReq req) {
        return friendShipGroupMemberService.addFriendShipGroupMember(req);
    }
}
