package com.catzhang.im.service.friendship.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.catzhang.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.catzhang.im.service.friendship.model.resp.ApproveFriendRequestResp;
import com.catzhang.im.service.friendship.model.resp.ReadFriendShipRequestResp;
import com.catzhang.im.service.friendship.service.FriendShipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/friendshipRequest")
public class FriendShipRequestController {

    @Autowired
    FriendShipRequestService friendShipRequestService;

    @PostMapping("approve")
    public ResponseVO<ApproveFriendRequestResp> approveFriendRequest(@RequestBody ApproveFriendRequestReq req) {
        return friendShipRequestService.approveFriendRequest(req);
    }

    @PutMapping("read")
    public ResponseVO<ReadFriendShipRequestResp> readFriendShipRequest(@RequestBody @Validated ReadFriendShipRequestReq req) {
        return friendShipRequestService.readFriendShipRequest(req);
    }
}
