package com.catzhang.im.service.friendship.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.catzhang.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.catzhang.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.catzhang.im.service.friendship.model.resp.ApproveFriendRequestResp;
import com.catzhang.im.service.friendship.model.resp.GetFriendShipRequestResp;
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

    @RequestMapping("approve")
    public ResponseVO<ApproveFriendRequestResp> approveFriendRequest(@RequestBody ApproveFriendRequestReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return friendShipRequestService.approveFriendRequest(req);
    }

    @RequestMapping("read")
    public ResponseVO<ReadFriendShipRequestResp> readFriendShipRequest(@RequestBody @Validated ReadFriendShipRequestReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipRequestService.readFriendShipRequest(req);
    }

    @RequestMapping("get")
    public ResponseVO<GetFriendShipRequestResp> getFriendShipRequest(@RequestBody @Validated GetFriendShipRequestReq req, Integer appId) {
        req.setAppId(appId);
        return friendShipRequestService.getFriendShipRequest(req);
    }
}
