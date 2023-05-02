package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum UserType {

    IM_USER(1),

    APP_ADMIN(100),
    ;

    private final int code;

    UserType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
