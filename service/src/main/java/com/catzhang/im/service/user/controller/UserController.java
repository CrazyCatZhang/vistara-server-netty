package com.catzhang.im.service.user.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.model.req.DeleteUserReq;
import com.catzhang.im.service.user.model.req.GetUserInfoReq;
import com.catzhang.im.service.user.model.req.ImportUserReq;
import com.catzhang.im.service.user.model.resp.DeleteUserResp;
import com.catzhang.im.service.user.model.resp.GetUserInfoResp;
import com.catzhang.im.service.user.model.resp.ImportUserResp;
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
}
