package com.catzhang.im.common.model;

import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class RequestBase {

    private Integer appId;

    private String operator;

    private Integer clientType;

    private String imei;
}
