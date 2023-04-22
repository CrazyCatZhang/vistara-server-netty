package com.catzhang.im.codec.pack.friendship;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class ReadAllFriendRequestPack {

    private String fromId;

    private Long sequence;
}
