package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */
public enum AllowGroupType {
    /**
     * 允许无需审批自由加入群组允许无需审批自由加入群组
     */
    NEED_APPROVAL(1),

    /**
     * 需要群主或管理员审批需要群主或管理员审批
     */
    NOT_NEED_APPROVAL(2),

    /**
     * 禁止任何人加入
     */
    FORBIDDEN_TO_JOIN(0);


    private final int code;

    AllowGroupType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
