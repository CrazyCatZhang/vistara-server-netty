package com.catzhang.im.service.user.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.model.req.ImportUserReq;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("import")
    public ResponseVO importUser(@RequestBody ImportUserReq req) {
        return userService.importUser(req);
    }
}
