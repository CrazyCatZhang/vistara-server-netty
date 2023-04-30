package com.catzhang.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
@TableName("im_group_message_history")
public class GroupMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String groupId;

    /**
     * messageBodyId
     */
    private Long messageKey;
    /**
     * 序列号
     */
    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;


}
