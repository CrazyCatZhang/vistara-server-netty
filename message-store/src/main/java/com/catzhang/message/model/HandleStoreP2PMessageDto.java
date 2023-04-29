package com.catzhang.message.model;

import com.catzhang.im.common.model.message.MessageContent;
import com.catzhang.message.dao.MessageBodyEntity;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class HandleStoreP2PMessageDto {

    private MessageContent messageContent;

    private MessageBodyEntity messageBodyEntity;

}
