package com.catzhang.im.common.enums;


import com.catzhang.im.common.exception.ApplicationExceptionEnum;

/**
 * @author crazycatzhang
 */

public enum ConversationErrorCode implements ApplicationExceptionEnum {

    CONVERSATION_UPDATE_PARAM_ERROR(50000, "會話修改參數錯誤"),


    ;

    private final int code;
    private final String error;

    ConversationErrorCode(int code, String error) {
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
