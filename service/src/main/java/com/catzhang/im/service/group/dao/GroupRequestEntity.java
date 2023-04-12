package com.catzhang.im.service.group.dao;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 
* @author crazycatzhang
 * @TableName im_group_request
*/

@Data
@TableName("im_group_request")
public class GroupRequestEntity  {

    /**
    * id
    */
    @NotNull(message="[id]不能为空")
    private Long id;
    /**
    * app_id
    */
    private Integer appId;
    /**
    * from_id
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String fromId;
    /**
    * to_group_id
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String toGroupId;
    /**
    * 是否已读 1已读
    */
    private Integer readStatus;
    /**
    * 来源
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Length(max= 20,message="编码长度不能超过20")
    private String addSource;
    /**
    * 申请加群验证信息
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Length(max= 50,message="编码长度不能超过50")
    private String addWording;
    /**
    * 审批状态 1同意 2拒绝
    */
    private Integer approveStatus;

    private Long createTime;

    private Long updateTime;

    private Long sequence;
}
