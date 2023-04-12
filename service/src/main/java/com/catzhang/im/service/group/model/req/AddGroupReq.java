package com.catzhang.im.service.group.model.req;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddGroupReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    @NotNull(message = "groupItem不能为空")
    private GroupDto groupItem;

}
