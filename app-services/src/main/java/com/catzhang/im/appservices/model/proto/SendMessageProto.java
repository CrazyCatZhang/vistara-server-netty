package com.catzhang.im.appservices.model.proto;

import lombok.Data;

/**
 * @author crazycatzhang
 */

@Data
public class SendMessageProto {

    private String fromId;

    private String toId;

    private Integer clientType;

    private String imei;

    private String messageBody;

}
