package com.catzhang.im.common.model.message;

import com.catzhang.im.common.model.ClientInfo;

/**
 * @author crazycatzhang
 */
public class MessageContent extends ClientInfo {

    private String messageId;

    private String fromId;

    private String toId;

    private String messageBody;

    private Long messageTime;

    private String extra;

    private Long messageKey;

    private long messageSequence;

}

