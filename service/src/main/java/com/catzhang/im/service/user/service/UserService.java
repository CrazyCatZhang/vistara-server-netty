package com.catzhang.im.service.user.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.user.model.req.DeleteUserReq;
import com.catzhang.im.service.user.model.req.GetUserInfoReq;
import com.catzhang.im.service.user.model.req.GetUserSequenceReq;
import com.catzhang.im.service.user.model.req.ImportUserReq;
import com.catzhang.im.service.user.model.resp.DeleteUserResp;
import com.catzhang.im.service.user.model.resp.GetUserInfoResp;
import com.catzhang.im.service.user.model.resp.GetUserSequenceResp;
import com.catzhang.im.service.user.model.resp.ImportUserResp;

/**
 * @author crazycatzhang
 */
public interface UserService {
    ResponseVO<ImportUserResp> importUser(ImportUserReq req);

    ResponseVO<DeleteUserResp> deleteUser(DeleteUserReq req);

    ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    ResponseVO<GetUserSequenceResp> getUserSequence(GetUserSequenceReq req);
}
