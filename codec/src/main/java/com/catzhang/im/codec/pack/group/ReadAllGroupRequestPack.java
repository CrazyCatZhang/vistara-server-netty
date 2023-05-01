package com.catzhang.im.codec.pack.group;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class ReadAllGroupRequestPack {

    private String fromId;

    private Long sequence;
}
