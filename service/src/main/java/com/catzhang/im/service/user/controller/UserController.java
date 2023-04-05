package com.catzhang.im.service.user.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.model.req.DeleteUserReq;
import com.catzhang.im.service.user.model.req.ImportUserReq;
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
    public ResponseVO importUser(@RequestBody ImportUserReq req) {
        return userService.importUser(req);
    }

    @DeleteMapping("delete")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req) {
        return userService.deleteUser(req);
    }
}
