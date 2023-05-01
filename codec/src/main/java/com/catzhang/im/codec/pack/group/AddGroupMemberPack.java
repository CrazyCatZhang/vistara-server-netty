package com.catzhang.im.codec.pack.group;

import lombok.Data;

import java.util.List;


/**
 * @author crazycatzhang
 */
@Data
public class AddGroupMemberPack {

    private String groupId;

    private List<String> members;

    private Long sequence;

}
