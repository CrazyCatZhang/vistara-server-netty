package com.catzhang.im.codec.pack.friendship;

import lombok.Data;

import java.util.List;


/**
 * @author crazycatzhang
 */
@Data
public class DeleteFriendGroupPack {
    public String fromId;

    private List<String> groupNames;

    /** 序列号*/
    private Long sequence;
}
