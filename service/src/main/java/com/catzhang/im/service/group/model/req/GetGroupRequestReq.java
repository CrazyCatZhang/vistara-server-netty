package com.catzhang.im.service.group.model.req;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetGroupRequestReq extends RequestBase {

    @NotBlank(message = "群组id不能为空")
    private String groupId;

}
