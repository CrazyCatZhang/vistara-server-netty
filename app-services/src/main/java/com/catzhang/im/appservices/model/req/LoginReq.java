package com.catzhang.im.appservices.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * @author crazycatzhang
 */
@Data
public class LoginReq {

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "请选择登录方式")
    //登录方式 1用户名+密码 2手机号+验证码
    private Integer loginType;

}
