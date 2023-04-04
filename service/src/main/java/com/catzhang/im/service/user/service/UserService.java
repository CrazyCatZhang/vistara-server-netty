package com.catzhang.im.service.user.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.dao.UserDataEntity;
import com.catzhang.im.service.user.model.req.ImportUserReq;

/**
 * @author crazycatzhang
 */
public interface UserService {
    public ResponseVO importUser(ImportUserReq req);
}
