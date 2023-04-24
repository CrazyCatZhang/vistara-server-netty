package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum UserForbiddenFlag {

    /**
     * 0 正常；1 禁用。
     */
    NORMAL(0),

    FORBIDDEN(1),
    ;

    private final int code;

    UserForbiddenFlag(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
