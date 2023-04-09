package com.catzhang.im.service.user.model.resp;

import com.catzhang.im.service.user.dao.UserDataEntity;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class GetSingleUserInfoResp {
    private UserDataEntity userDataEntity;
}
