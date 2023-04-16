package com.catzhang.im.common.model;

import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class UserSession {

    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 端的标识
     */
    private Integer clientType;

    //前端sdk版本号
    private Integer version;

    //连接状态 1在线 2离线
    private Integer connectState;

    private Integer brokerId;

    private String brokerHost;

    private String imei;

}
