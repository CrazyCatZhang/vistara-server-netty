package com.catzhang.im.common.enums;


import com.catzhang.im.common.exception.ApplicationExceptionEnum;

/**
 * @author crazycatzhang
 */

public enum MessageErrorCode implements ApplicationExceptionEnum {


    SENDER_IS_MUTE(50002, "发送方被禁言"),

    SENDER_IS_FORBIDDEN(50003, "发送方被禁用"),


    MESSAGEBODY_IS_NOT_EXIST(50003, "消息体不存在"),

    MESSAGE_RECALL_TIME_OUT(50004, "消息已超过可撤回时间"),

    MESSAGE_IS_RECALLED(50005, "消息已被撤回"),

    ;

    private final int code;
    private final String error;

    MessageErrorCode(int code, String error) {
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
