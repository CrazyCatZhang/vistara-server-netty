package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum ApproveFriendRequestStatus {
    /**
     * 1 同意；2 拒绝。
     */
    AGREE(1),

    REJECT(2),
    ;

    private final int code;

    ApproveFriendRequestStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
