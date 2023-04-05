package com.catzhang.im.service.user.model.resp;

import com.catzhang.im.service.user.dao.UserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
public class GetUserInfoResp {

    private List<UserDataEntity> userDataItems;

    private List<String> failUser;

}
