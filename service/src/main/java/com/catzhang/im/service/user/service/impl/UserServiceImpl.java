package com.catzhang.im.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.DelFlag;
import com.catzhang.im.common.enums.UserErrorCode;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.user.dao.UserDataEntity;
import com.catzhang.im.service.user.dao.mapper.UserDataMapper;
import com.catzhang.im.service.user.model.req.*;
import com.catzhang.im.service.user.model.resp.*;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author crazycatzhang
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDataMapper userDataMapper;

    @Override
    public ResponseVO<ImportUserResp> importUser(ImportUserReq req) {

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
    public ResponseVO<DeleteUserResp> deleteUser(DeleteUserReq req) {
        UserDataEntity userDataEntity = new UserDataEntity();
        userDataEntity.setDelFlag(DelFlag.DELETE.getCode());

        List<String> successIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();

        for (String userId :
                req.getUserIds()) {
            LambdaQueryWrapper<UserDataEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserDataEntity::getUserId, userId)
                    .eq(UserDataEntity::getAppId, req.getAppId());
            int delete;
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

    @Override
    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        LambdaQueryWrapper<UserDataEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(UserDataEntity::getUserId, req.getUserIds())
                .eq(UserDataEntity::getAppId, req.getAppId());

        List<UserDataEntity> userDataEntities = userDataMapper.selectList(lambdaQueryWrapper);
        Map<String, UserDataEntity> userDataMap = new HashMap<>();

        userDataEntities.forEach(userDataEntity -> userDataMap.put(userDataEntity.getUserId(), userDataEntity));
        List<String> failUser = new ArrayList<>();
        req.getUserIds().forEach(uid -> {
            if (!userDataMap.containsKey(uid)) {
                failUser.add(uid);
            }
        });
        GetUserInfoResp getUserInfoResp = new GetUserInfoResp();
        getUserInfoResp.setUserDataItems(userDataEntities);
        getUserInfoResp.setFailUser(failUser);
        return ResponseVO.successResponse(getUserInfoResp);
    }

    @Override
    public ResponseVO<GetSingleUserInfoResp> getSingleUserInfo(GetSingleUserInfoReq req) {
        LambdaQueryWrapper<UserDataEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDataEntity::getUserId, req.getUserId())
                .eq(UserDataEntity::getAppId, req.getAppId());

        UserDataEntity userDataEntity = userDataMapper.selectOne(lambdaQueryWrapper);

        if (userDataEntity == null) {
            return ResponseVO.errorResponse(UserErrorCode.USER_IS_NOT_EXIST);
        }

        GetSingleUserInfoResp getSingleUserInfoResp = new GetSingleUserInfoResp();
        getSingleUserInfoResp.setUserDataEntity(userDataEntity);

        return ResponseVO.successResponse(getSingleUserInfoResp);
    }

    @Override
    @Transactional
    public ResponseVO<ModifyUserInfoResp> modifyUserInfo(ModifyUserInfoReq req) {
        LambdaQueryWrapper<UserDataEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDataEntity::getAppId, req.getAppId())
                .eq(UserDataEntity::getUserId, req.getUserId());

        UserDataEntity userDataEntity = userDataMapper.selectOne(lambdaQueryWrapper);
        if (userDataEntity == null) {
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }

        BeanUtils.copyProperties(req, userDataEntity);

        int update = userDataMapper.update(userDataEntity, lambdaQueryWrapper);

        ModifyUserInfoResp modifyUserInfoResp = new ModifyUserInfoResp();

        if (update == 1) {
            modifyUserInfoResp.setUserDataEntity(userDataEntity);
            return ResponseVO.successResponse(modifyUserInfoResp);
        }

        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }
}
