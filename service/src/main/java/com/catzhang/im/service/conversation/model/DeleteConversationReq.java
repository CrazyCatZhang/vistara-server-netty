package com.catzhang.im.service.conversation.model;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;


/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeleteConversationReq extends RequestBase {

    @NotBlank(message = "会话id不能为空")
    private String conversationId;

    @NotBlank(message = "fromId不能为空")
    private String fromId;

}
