package com.catzhang.im.appservices.controller;


import com.catzhang.im.appservices.common.ResponseVO;
import com.catzhang.im.appservices.dao.User;
import com.catzhang.im.appservices.model.req.LoginReq;
import com.catzhang.im.appservices.model.req.RegisterReq;
import com.catzhang.im.appservices.model.resp.LoginResp;
import com.catzhang.im.appservices.service.LoginService;
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
@RequestMapping
public class LoginController {

    @Autowired
    LoginService loginService;

    @PostMapping("/login")
    public ResponseVO<LoginResp> login(@RequestBody @Validated LoginReq req) {
        return loginService.login(req);
    }

    @PostMapping("/register")
    public ResponseVO<User> register(@RequestBody @Validated RegisterReq req) {
        return loginService.register(req);
    }

}
