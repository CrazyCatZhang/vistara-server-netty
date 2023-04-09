package com.catzhang.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
@TableName("im_friendship_group")
public class FriendShipGroupEntity {
    @TableId(value = "group_id")
    private Long groupId;

    private String fromId;

    private Integer appId;

    private String groupName;
    /**
     * 备注
     */
    private Long createTime;

    /**
     * 备注
     */
    private Long updateTime;

    /**
     * 序列号
     */
    private Long sequence;

    @TableLogic
    private int delFlag;
}
