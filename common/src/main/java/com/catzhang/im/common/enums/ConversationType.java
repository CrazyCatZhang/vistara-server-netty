package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum ConversationType {

    /**
     * 0 单聊 1群聊 2机器人 3公众号
     */
    P2P(0),

    GROUP(1),

    ROBOT(2),
    ;

    private final int code;

    ConversationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
