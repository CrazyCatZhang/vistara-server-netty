package com.catzhang.im.service.user.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.model.req.*;
import com.catzhang.im.service.user.model.resp.*;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

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

    @GetMapping("getUserSequence")
    public ResponseVO<GetUserSequenceResp> getUserSequence(@RequestBody GetUserSequenceReq req) {
        return userService.getUserSequence(req);
    }

    @PutMapping("modifyUserInfo")
    public ResponseVO<ModifyUserInfoResp> modifyUserInfo(@RequestBody @Validated ModifyUserInfoReq req) {
        return userService.modifyUserInfo(req);
    }
}
