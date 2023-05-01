package com.catzhang.im.codec.pack.group;

import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class AddGroupRequestPack {

    /**
     * id
     */
    private Long id;
    /**
     * app_id
     */
    private Integer appId;
    /**
     * from_id
     */
    private String fromId;
    /**
     * to_group_id
     */
    private String toGroupId;
    /**
     * 是否已读 1已读
     */
    private Integer readStatus;
    /**
     * 来源
     */
    private String addSource;
    /**
     * 申请加群验证信息
     */
    private String addWording;
    /**
     * 审批状态 1同意 2拒绝
     */
    private Integer approveStatus;

    private Long createTime;

    private Long updateTime;

    private Long sequence;

}
