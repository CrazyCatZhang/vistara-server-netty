package com.catzhang.im.service.user.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.route.RouteHandle;
import com.catzhang.im.service.user.model.req.*;
import com.catzhang.im.service.user.model.resp.*;
import com.catzhang.im.service.user.service.UserService;
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

    @PostMapping("import")
    public ResponseVO<ImportUserResp> importUser(@RequestBody ImportUserReq req) {
        return userService.importUser(req);
    }

    @DeleteMapping("delete")
    public ResponseVO<DeleteUserResp> deleteUser(@RequestBody @Validated DeleteUserReq req) {
        return userService.deleteUser(req);
    }

    @GetMapping("getUserInfo")
    public ResponseVO<GetUserInfoResp> getUserInfo(@RequestBody GetUserInfoReq req) {
        return userService.getUserInfo(req);
    }

    @GetMapping("getSingleUserInfo")
    public ResponseVO<GetSingleUserInfoResp> getUserSequence(@RequestBody GetSingleUserInfoReq req) {
        return userService.getSingleUserInfo(req);
    }

    @PutMapping("modifyUserInfo")
    public ResponseVO<ModifyUserInfoResp> modifyUserInfo(@RequestBody @Validated ModifyUserInfoReq req) {
        return userService.modifyUserInfo(req);
    }

    @PostMapping("login")
    public ResponseVO<LoginResp> login(@RequestBody @Validated LoginReq req) {
        return userService.login(req);
    }

    @PostMapping("getUserSequence")
    public ResponseVO<Map<Object, Object>> getUserSequence(@RequestBody @Validated GetUserSequenceReq req) {
        return userService.getUserSequence(req);
    }
}
