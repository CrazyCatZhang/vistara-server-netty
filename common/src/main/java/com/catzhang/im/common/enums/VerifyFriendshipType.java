package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum VerifyFriendshipType {
    /**
     * 1 单向校验；2双向校验。
     */
    SINGLE(1),

    BOTH(2),
    ;

    private int type;

    VerifyFriendshipType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
