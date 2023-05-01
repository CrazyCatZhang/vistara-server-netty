package com.catzhang.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
@TableName("im_friendship_group_member")
public class FriendShipGroupMemberEntity {

    @TableId(value = "group_id")
    private Long groupId;

    private String toId;

    private Long sequence;

}
