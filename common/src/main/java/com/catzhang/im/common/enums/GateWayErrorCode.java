package com.catzhang.im.common.enums;


import com.catzhang.im.common.exception.ApplicationExceptionEnum;

/**
 * @author crazycatzhang
 */

public enum GateWayErrorCode implements ApplicationExceptionEnum {

    USER_SIGN_NOT_EXIST(60000, "用户签名不存在"),

    APPID_NOT_EXIST(60001, "appId不存在"),

    OPERATOR_NOT_EXIST(60002, "操作人不存在"),

    USER_SIGN_IS_ERROR(60003, "用户签名不正确"),

    USER_SIGN_OPERATE_NOT_MATE(60005, "用户签名与操作人不匹配"),

    USER_SIGN_IS_EXPIRED(60004, "用户签名已过期"),

    ;

    private final int code;
    private final String error;

    GateWayErrorCode(int code, String error) {
        this.code = code;
        this.error = error;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getError() {
        return this.error;
    }

}
