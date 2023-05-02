package com.catzhang.im.appservices.service;


import com.catzhang.im.appservices.common.ResponseVO;
import com.catzhang.im.appservices.dao.User;
import com.catzhang.im.appservices.model.req.LoginReq;
import com.catzhang.im.appservices.model.req.RegisterReq;
import com.catzhang.im.appservices.model.resp.LoginResp;


/**
 * @author crazycatzhang
 */
public interface LoginService {

    ResponseVO<LoginResp> login(LoginReq req);

    ResponseVO<User> register(RegisterReq req);
}
