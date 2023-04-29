package com.catzhang.im.common.model.message;

import com.catzhang.im.common.model.ClientInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageReceiveAckContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;


}
