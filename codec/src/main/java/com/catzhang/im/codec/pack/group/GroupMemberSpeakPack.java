package com.catzhang.im.codec.pack.group;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class GroupMemberSpeakPack {

    private String groupId;

    private String memberId;

    private Long speakDate;

}
