package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum DelFlag {
    NORMAL(0),

    DELETE(1),
    ;

    private int code;

    DelFlag(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
