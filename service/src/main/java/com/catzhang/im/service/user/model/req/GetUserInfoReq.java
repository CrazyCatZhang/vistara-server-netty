package com.catzhang.im.service.user.model.req;

import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author crazycatzhang
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUserInfoReq extends RequestBase {
    private List<String> userIds;
}
