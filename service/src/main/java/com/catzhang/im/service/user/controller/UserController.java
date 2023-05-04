package com.catzhang.im.service.user.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.route.RouteHandle;
import com.catzhang.im.service.user.model.req.*;
import com.catzhang.im.service.user.model.resp.*;
import com.catzhang.im.service.user.service.UserService;
import com.catzhang.im.service.user.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    RouteHandle routeHandle;

    @Autowired
    UserStatusService userStatusService;

    @RequestMapping("import")
    public ResponseVO<ImportUserResp> importUser(@RequestBody ImportUserReq req, Integer appId) {
        req.setAppId(appId);
        return userService.importUser(req);
    }

    @RequestMapping("delete")
    public ResponseVO<DeleteUserResp> deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return userService.deleteUser(req);
    }

    @RequestMapping("getUserInfo")
    public ResponseVO<GetUserInfoResp> getUserInfo(@RequestBody GetUserInfoReq req, Integer appId) {
        req.setAppId(appId);
        return userService.getUserInfo(req);
    }

    @RequestMapping("getSingleUserInfo")
    public ResponseVO<GetSingleUserInfoResp> getUserSequence(@RequestBody GetSingleUserInfoReq req, Integer appId) {
        req.setAppId(appId);
        return userService.getSingleUserInfo(req);
    }

    @RequestMapping("modifyUserInfo")
    public ResponseVO<ModifyUserInfoResp> modifyUserInfo(@RequestBody @Validated ModifyUserInfoReq req, Integer appId) {
        req.setAppId(appId);
        return userService.modifyUserInfo(req);
    }

    @RequestMapping("login")
    public ResponseVO<LoginResp> login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);
        return userService.login(req);
    }

    @RequestMapping("getUserSequence")
    public ResponseVO<Map<Object, Object>> getUserSequence(@RequestBody @Validated GetUserSequenceReq req, Integer appId) {
        req.setAppId(appId);
        return userService.getUserSequence(req);
    }

    @RequestMapping("subscribeUserOnlineStatus")
    public ResponseVO getUserSequence(@RequestBody @Validated SubscribeUserOnlineStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        userStatusService.subscribeUserOnlineStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("setUserCustomerStatus")
    public ResponseVO setUserCustomerStatus(@RequestBody @Validated SetUserCustomerStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        userStatusService.setUserCustomerStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("queryFriendOnlineStatus")
    public ResponseVO<Map<String, UserOnlineStatusResp>> queryFriendOnlineStatus(@RequestBody @Validated
                                                                                 PullFriendOnlineStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return userStatusService.queryFriendOnlineStatus(req);
    }

    @RequestMapping("queryUserOnlineStatus")
    public ResponseVO<Map<String, UserOnlineStatusResp>> queryUserOnlineStatus(@RequestBody @Validated
                                                                               PullUserOnlineStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return userStatusService.queryUserOnlineStatus(req);
    }
}
