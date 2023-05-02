package com.catzhang.im.appservices.service.impl;


import com.catzhang.im.appservices.common.ResponseVO;
import com.catzhang.im.appservices.config.AppConfig;
import com.catzhang.im.appservices.dao.User;
import com.catzhang.im.appservices.enums.ErrorCode;
import com.catzhang.im.appservices.enums.LoginTypeEnum;
import com.catzhang.im.appservices.enums.RegisterTypeEnum;
import com.catzhang.im.appservices.model.req.LoginReq;
import com.catzhang.im.appservices.model.req.RegisterReq;
import com.catzhang.im.appservices.model.resp.LoginResp;
import com.catzhang.im.appservices.service.LoginService;
import com.catzhang.im.appservices.service.UserService;
import com.catzhang.im.appservices.utils.SignApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    UserService userService;

    @Autowired
    AppConfig appConfig;

    @Override
    public ResponseVO<LoginResp> login(LoginReq req) {

        LoginResp loginResp = new LoginResp();

        if (LoginTypeEnum.USERNAME_PASSWORD.getCode() == req.getLoginType()) {
            ResponseVO<User> userResp = userService.getUserByUserNameAndPassword(req.getUserName(), req.getPassword());
            if (userResp.isOk()) {
                User user = userResp.getData();
                SignApi signApi = new SignApi(appConfig.getAppId(),
                        appConfig.getPrivateKey());
                String s = signApi.genUserSign(user.getUserId(),
                        500000);
                loginResp.setImUserSign(s);
                loginResp.setUserSign("asdasdsd");
                loginResp.setUserId(user.getUserId());
            } else if (userResp.getCode() == ErrorCode.USER_NOT_EXIST.getCode()) {
                return ResponseVO.errorResponse(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
            } else {
                return ResponseVO.errorResponse(userResp.getCode(), userResp.getMsg());
            }

        } else if (LoginTypeEnum.SMS_CODE.getCode() == req.getLoginType()) {
            String key = "CatZhang";
        }

        loginResp.setAppId(appConfig.getAppId());
        return ResponseVO.successResponse(loginResp);
    }


    @Override
    @Transactional
    public ResponseVO<User> register(RegisterReq req) {
        if (RegisterTypeEnum.USERNAME.getCode() == req.getRegisterType()) {
            ResponseVO<User> userByUserName = userService.getUserByUserName(req.getUserName());
            if (userByUserName.isOk()) {
                return ResponseVO.errorResponse(ErrorCode.REGISTER_ERROR);
            }
            return userService.registerUser(req);
        }
        return ResponseVO.successResponse();
    }


}
