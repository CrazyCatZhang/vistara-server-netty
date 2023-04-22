package com.catzhang.im.codec.pack.friendship;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class DeleteFriendGroupPack {
    public String fromId;

    private String groupName;

    /** 序列号*/
    private Long sequence;
}
