package com.catzhang.im.common.model.message;

import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class VerifySendMessageReq {

    private String fromId;

    private String toId;

    private Integer appId;

    private Integer command;

}
