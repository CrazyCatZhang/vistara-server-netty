package com.catzhang.im.service.conversation.model;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private String fromId;


}
