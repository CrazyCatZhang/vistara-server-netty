package com.catzhang.im.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.DelFlagEnum;
import com.catzhang.im.common.enums.UserErrorCode;
import com.catzhang.im.service.user.dao.UserDataEntity;
import com.catzhang.im.service.user.dao.mapper.UserDataMapper;
import com.catzhang.im.service.user.model.req.DeleteUserReq;
import com.catzhang.im.service.user.model.req.ImportUserReq;
import com.catzhang.im.service.user.model.resp.DeleteUserResp;
import com.catzhang.im.service.user.model.resp.ImportUserResp;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDataMapper userDataMapper;

    @Override
    public ResponseVO importUser(ImportUserReq req) {

        if (req.getUserData().size() > 100) {
            return ResponseVO.errorResponse(UserErrorCode.IMPORT_SIZE_BEYOND);
        }

        ImportUserResp importUserResp = new ImportUserResp();
        List<String> successIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();

        for (UserDataEntity userData : req.getUserData()) {
            try {
                userData.setAppId(req.getAppId());
                int insert = userDataMapper.insert(userData);
                if (insert == 1) {
                    successIds.add(userData.getUserId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorIds.add(userData.getUserId());
            }
        }

        importUserResp.setSuccessIds(successIds);
        importUserResp.setErrorIds(errorIds);

        return ResponseVO.successResponse(importUserResp);
    }

    @Override
    public ResponseVO deleteUser(DeleteUserReq req) {
        UserDataEntity userDataEntity = new UserDataEntity();
        userDataEntity.setDelFlag(DelFlagEnum.DELETE.getCode());

        List<String> successIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();

        for (String userId :
                req.getUserIds()) {
            LambdaQueryWrapper<UserDataEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.like(UserDataEntity::getUserId, userId)
                    .like(UserDataEntity::getAppId, req.getAppId());
            int delete = 0;
            try {
                delete = userDataMapper.delete(lambdaQueryWrapper);
                if (delete > 0) {
                    successIds.add(userId);
                } else {
                    errorIds.add(userId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorIds.add(userId);
            }
        }

        DeleteUserResp deleteUserResp = new DeleteUserResp();
        deleteUserResp.setSuccessIds(successIds);
        deleteUserResp.setErrorIds(errorIds);

        return ResponseVO.successResponse(deleteUserResp);
    }

}
