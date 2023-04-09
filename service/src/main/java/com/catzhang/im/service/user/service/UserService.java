package com.catzhang.im.service.user.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.model.req.*;
import com.catzhang.im.service.user.model.resp.*;

/**
 * @author crazycatzhang
 */
public interface UserService {
    ResponseVO<ImportUserResp> importUser(ImportUserReq req);

    ResponseVO<DeleteUserResp> deleteUser(DeleteUserReq req);

    ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    ResponseVO<GetSingleUserInfoResp> getSingleUserInfo(GetSingleUserInfoReq req);

    ResponseVO<ModifyUserInfoResp> modifyUserInfo(ModifyUserInfoReq req);
}
