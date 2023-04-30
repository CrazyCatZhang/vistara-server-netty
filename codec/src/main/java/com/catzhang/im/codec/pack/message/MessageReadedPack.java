package com.catzhang.im.codec.pack.message;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class MessageReadedPack {

    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    private Integer conversationType;
}
