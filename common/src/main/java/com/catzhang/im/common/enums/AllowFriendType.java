package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */
public enum AllowFriendType {
    /**
     * 验证
     */
    NEED(2),

    /**
     * 不需要验证
     */
    NOT_NEED(1),

    ;


    private int code;

    AllowFriendType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
