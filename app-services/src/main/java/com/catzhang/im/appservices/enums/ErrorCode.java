package com.catzhang.im.appservices.enums;


import com.catzhang.im.appservices.exception.ApplicationExceptionEnum;

/**
 * @author crazycatzhang
 */

public enum ErrorCode implements ApplicationExceptionEnum {

    USER_NOT_EXIST(10000, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(10001, "用户名或密码错误"),
    MOBILE_IS_REGISTER(10002, "该手机号已注册了用户"),

    REGISTER_ERROR(10003, "注册失败"),

    REPORT_TAG_IS_NOT_EXIST(10004, "举报标签不存在"),

    REDPACKET_IS_NOT_EXIST(10005, "红包不存在"),
    USER_REDPACKET_IS_OPEN(10006, "用户已抢过该红包"),
    REDPACKET_IS_EXPIRE(10007, "红包已过期"),
    REDPACKET_IS_FINISH(10008, "红包已抢完"),
    REDPACKET_IS_HOT(10009, "红包火爆请稍后再试"),
    TRYLOCREDPACKET_INTERRUPTED(10010, "抢红包时被中断了"),
    ;

    private final int code;
    private final String error;

    ErrorCode(int code, String error) {
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
