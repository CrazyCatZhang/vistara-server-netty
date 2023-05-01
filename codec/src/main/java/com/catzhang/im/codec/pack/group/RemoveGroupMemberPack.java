package com.catzhang.im.codec.pack.group;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class RemoveGroupMemberPack {

    private String groupId;

    private String member;

    private Long sequence;

}
