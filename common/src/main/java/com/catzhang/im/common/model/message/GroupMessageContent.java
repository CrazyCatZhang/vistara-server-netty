package com.catzhang.im.common.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupMessageContent extends MessageContent {

    private String groupId;

    private List<String> memberId;

}
