package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum ImUrlRouteWay {

    /**
     * 随机
     */
    RANDOM(1, "com.catzhang.im.common.route.algorithm.random.RandomHandle"),


    /**
     * 1.轮训
     */
    LOOP(2, "com.catzhang.im.common.route.algorithm.loop.LoopHandle"),

    /**
     * HASH
     */
    HASH(3, "com.catzhang.im.common.route.algorithm.consistenthash.ConsistentHashHandle"),
    ;


    private final int code;
    private final String clazz;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     *
     * @param ordinal
     * @return
     */
    public static ImUrlRouteWay getHandler(int ordinal) {
        for (int i = 0; i < ImUrlRouteWay.values().length; i++) {
            if (ImUrlRouteWay.values()[i].getCode() == ordinal) {
                return ImUrlRouteWay.values()[i];
            }
        }
        return null;
    }

    ImUrlRouteWay(int code, String clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public String getClazz() {
        return clazz;
    }

    public int getCode() {
        return code;
    }
}
