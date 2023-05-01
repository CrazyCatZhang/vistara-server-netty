package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum ConnectStatus {


    /**
     * 管道链接状态,1=在线，2=离线。。
     */
    ONLINE_STATUS(1),

    OFFLINE_STATUS(2),
    ;

    private final Integer code;

    ConnectStatus(Integer code){
        this.code=code;
    }

    public Integer getCode() {
        return code;
    }

}
