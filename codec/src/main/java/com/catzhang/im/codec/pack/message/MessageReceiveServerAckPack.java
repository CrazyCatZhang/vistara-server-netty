package com.catzhang.im.codec.pack.message;

import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class MessageReceiveServerAckPack {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

    private Boolean serverSend;

}
