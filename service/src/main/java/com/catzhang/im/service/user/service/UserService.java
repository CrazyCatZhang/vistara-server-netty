package com.catzhang.im.service.user.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.model.req.DeleteUserReq;
import com.catzhang.im.service.user.model.req.ImportUserReq;

/**
 * @author crazycatzhang
 */
public interface UserService {
    ResponseVO importUser(ImportUserReq req);

    ResponseVO deleteUser(DeleteUserReq req);
}
