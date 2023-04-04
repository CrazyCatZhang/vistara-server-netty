package com.catzhang.im.service.user.model.req;

import com.catzhang.im.common.model.RequestBase;
import com.catzhang.im.service.user.dao.UserDataEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImportUserReq extends RequestBase {
    private List<UserDataEntity> userData;
}
