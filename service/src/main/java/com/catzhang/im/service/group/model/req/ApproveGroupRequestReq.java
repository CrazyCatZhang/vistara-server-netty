package com.catzhang.im.service.group.model.req;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApproveGroupRequestReq extends RequestBase {

    private Long id;

    //1同意 2拒绝
    private Integer status;

}
