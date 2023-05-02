package com.catzhang.im.appservices.enums;

/**
 * @author crazycatzhang
 */

public enum RegisterTypeEnum {

    /**
     * 1 username；2 MOBILE。
     */
    USERNAME(1),

    MOBILE(2),
    ;

    private final int code;

    RegisterTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
