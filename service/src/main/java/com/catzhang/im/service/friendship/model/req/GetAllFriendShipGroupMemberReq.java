package com.catzhang.im.service.friendship.model.req;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetAllFriendShipGroupMemberReq extends RequestBase {

    @NotNull(message = "groupId不能为空")
    private Long groupId;

}
