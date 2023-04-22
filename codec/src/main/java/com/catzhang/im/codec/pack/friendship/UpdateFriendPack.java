package com.catzhang.im.codec.pack.friendship;

import lombok.Data;



/**
 * @author crazycatzhang
 */
@Data
public class UpdateFriendPack {

    public String fromId;

    private String toId;

    private String remark;

    private Long sequence;
}
