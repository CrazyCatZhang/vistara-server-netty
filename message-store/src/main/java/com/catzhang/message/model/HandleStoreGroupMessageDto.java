package com.catzhang.message.model;

import com.catzhang.im.common.model.message.GroupMessageContent;
import com.catzhang.message.dao.MessageBodyEntity;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class HandleStoreGroupMessageDto {

    private GroupMessageContent groupMessageContent;

    private MessageBodyEntity messageBodyEntity;

}
