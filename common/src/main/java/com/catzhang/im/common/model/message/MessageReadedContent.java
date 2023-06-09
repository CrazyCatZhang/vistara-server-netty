package com.catzhang.im.common.model.message;

import com.catzhang.im.common.model.ClientInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageReadedContent extends ClientInfo {

    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    private Integer conversationType;

}
