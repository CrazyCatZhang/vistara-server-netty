package com.catzhang.im.service.user.model.req;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModifyUserInfoReq extends RequestBase {
    // 用户id
    @NotEmpty(message = "用户id不能为空")
    private String userId;

    // 用户名称
    private String nickName;

    //位置
    private String location;

    //生日
    private String birthDay;

    private String password;

    // 头像
    private String photo;

    // 性别
    private Integer userSex;

    // 个性签名
    private String selfSignature;

    // 加好友验证类型（Friend_AllowType） 1需要验证
    private Integer friendAllowType;

    private String extra;
}
