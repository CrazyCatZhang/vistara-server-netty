package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum DelFlagEnum {
    NORMAL(0),

    DELETE(1),
    ;

    private int code;

    DelFlagEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
