package com.catzhang.im.codec.pack.friendship;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class DeleteFriendPack {

    private String fromId;

    private String toId;

    private Long sequence;
}
