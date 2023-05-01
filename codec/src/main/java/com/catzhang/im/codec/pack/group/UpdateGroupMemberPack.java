package com.catzhang.im.codec.pack.group;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class UpdateGroupMemberPack {

    private String groupId;

    private String memberId;

    private String alias;

    private String extra;

    private Long sequence;
}
