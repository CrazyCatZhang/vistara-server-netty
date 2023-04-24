package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum UserSilentFlag {

    /**
     * 0 正常；1 禁言。
     */
    NORMAL(0),

    MUTE(1),
    ;

    private final int code;

    UserSilentFlag(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
