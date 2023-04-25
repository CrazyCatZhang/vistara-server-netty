package com.catzhang.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
@TableName("im_message_body")
public class MessageBodyEntity {

    private Integer appId;

    /**
     * messageBodyId
     */
    @TableId(value = "message_key")
    private Long messageKey;

    /**
     * messageBody
     */
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;

}
