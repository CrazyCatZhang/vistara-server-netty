package com.catzhang.im.service.friendship.model.req;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetFriendShipGroupReq extends RequestBase {
    @NotBlank(message = "用户Id不能为空")
    private String fromId;

    @NotBlank(message = "分组名称不能为空")
    private String groupName;
}
