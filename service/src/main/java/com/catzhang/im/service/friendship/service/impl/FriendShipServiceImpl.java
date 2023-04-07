package com.catzhang.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.AllowFriendType;
import com.catzhang.im.common.enums.FriendShipErrorCode;
import com.catzhang.im.common.enums.FriendShipStatus;
import com.catzhang.im.service.friendship.dao.FriendShipEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipMapper;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipService;
import com.catzhang.im.service.user.dao.UserDataEntity;
import com.catzhang.im.service.user.model.req.GetUserSequenceReq;
import com.catzhang.im.service.user.model.resp.GetUserSequenceResp;
import com.catzhang.im.service.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class FriendShipServiceImpl implements FriendShipService {

    @Autowired
    FriendShipMapper friendShipMapper;

    @Autowired
    UserService userService;

    @Override
    public ResponseVO<ImportFriendShipResp> importFriendShip(ImportFriendShipReq req) {
        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp importFriendShipResp = new ImportFriendShipResp();
        List<String> successIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();
        req.getFriendItem().forEach(item -> {
            FriendShipEntity friendShipEntity = new FriendShipEntity();
            BeanUtils.copyProperties(item, friendShipEntity);
            friendShipEntity.setAppId(req.getAppId());
            friendShipEntity.setFromId(req.getFromId());
            try {
                int insert = friendShipMapper.insert(friendShipEntity);
                if (insert == 1) {
                    successIds.add(item.getToId());
                } else {
                    errorIds.add(item.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorIds.add(item.getToId());
            }
        });

        importFriendShipResp.setSuccessIds(successIds);
        importFriendShipResp.setErrorIds(errorIds);
        return ResponseVO.successResponse(importFriendShipResp);
    }

    @Override
    public ResponseVO<AddFriendShipResp> addFriendShip(AddFriendShipReq req) {
        AddFriendShipResp addFriendShipResp = new AddFriendShipResp();
        GetUserSequenceReq getUserSequenceReq = new GetUserSequenceReq();
        getUserSequenceReq.setUserId(req.getFromId());
        getUserSequenceReq.setAppId(req.getAppId());
        ResponseVO<GetUserSequenceResp> fromUserInfo = userService.getUserSequence(getUserSequenceReq);
        if (!fromUserInfo.isOk()) {
            return ResponseVO.errorResponse(fromUserInfo.getCode(), fromUserInfo.getMsg());
        }

        getUserSequenceReq.setUserId(req.getToItem().getToId());
        ResponseVO<GetUserSequenceResp> toUserInfo = userService.getUserSequence(getUserSequenceReq);
        if (!toUserInfo.isOk()) {
            return ResponseVO.errorResponse(toUserInfo.getCode(), toUserInfo.getMsg());
        }

        UserDataEntity userDataEntity = toUserInfo.getData().getUserDataEntity();
        if (userDataEntity.getFriendAllowType() != null && userDataEntity.getFriendAllowType() == AllowFriendType.NOT_NEED.getCode()) {
            HandleAddFriendShipReq handleAddFriendShipReq = new HandleAddFriendShipReq();
            BeanUtils.copyProperties(req, handleAddFriendShipReq);
            ResponseVO<HandleAddFriendShipResp> handleAddFriendShipRespResponseVO = this.handleAddFriendShip(handleAddFriendShipReq);
            if (!handleAddFriendShipRespResponseVO.isOk()) {
                return ResponseVO.errorResponse(handleAddFriendShipRespResponseVO.getCode(), handleAddFriendShipRespResponseVO.getMsg());
            }
            addFriendShipResp.setFriendShipEntity(handleAddFriendShipRespResponseVO.getData().getFriendShipEntities());
            return ResponseVO.successResponse(addFriendShipResp);
        } else {

        }
        return ResponseVO.successResponse(addFriendShipResp);
    }

    @Override
    @Transactional
    public ResponseVO<HandleAddFriendShipResp> handleAddFriendShip(HandleAddFriendShipReq req) {
        HandleAddFriendShipResp handleAddFriendShipResp = new HandleAddFriendShipResp();
        LambdaQueryWrapper<FriendShipEntity> fromLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fromLambdaQueryWrapper.like(FriendShipEntity::getAppId, req.getAppId())
                .like(FriendShipEntity::getFromId, req.getFromId())
                .like(FriendShipEntity::getToId, req.getToItem().getToId());
        FriendShipEntity fromItem = friendShipMapper.selectOne(fromLambdaQueryWrapper);
        if (fromItem == null) {
            fromItem = new FriendShipEntity();
            fromItem.setAppId(req.getAppId());
            fromItem.setFromId(req.getFromId());
            BeanUtils.copyProperties(req.getToItem(), fromItem);
            fromItem.setStatus(FriendShipStatus.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = friendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        } else {
            if (fromItem.getStatus() == FriendShipStatus.FRIEND_STATUS_NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else {
                FriendDto toItem = req.getToItem();
                if (StringUtils.isNotBlank(toItem.getRemark())) {
                    fromItem.setRemark(toItem.getRemark());
                }
                if (StringUtils.isNotBlank(toItem.getAddSource())) {
                    fromItem.setAddSource(toItem.getAddSource());
                }
                if (StringUtils.isNotBlank(toItem.getExtra())) {
                    fromItem.setExtra(toItem.getExtra());
                }
                fromItem.setStatus(FriendShipStatus.FRIEND_STATUS_NORMAL.getCode());
                int update = friendShipMapper.update(fromItem, fromLambdaQueryWrapper);
                if (update != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
            }
        }

        LambdaQueryWrapper<FriendShipEntity> toLambdaQueryWrapper = new LambdaQueryWrapper<>();
        toLambdaQueryWrapper.like(FriendShipEntity::getAppId, req.getAppId())
                .like(FriendShipEntity::getFromId, req.getToItem().getToId())
                .like(FriendShipEntity::getToId, req.getFromId());
        FriendShipEntity toItem = friendShipMapper.selectOne(toLambdaQueryWrapper);
        if (toItem == null) {
            toItem = new FriendShipEntity();
            toItem.setAppId(req.getAppId());
            toItem.setFromId(req.getToItem().getToId());
//            BeanUtils.copyProperties(req.getToItem(), toItem);
            toItem.setToId(req.getFromId());
            toItem.setStatus(FriendShipStatus.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
            int inset = friendShipMapper.insert(toItem);
            if (inset != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        } else {
            if (toItem.getStatus() != FriendShipStatus.FRIEND_STATUS_NORMAL.getCode()) {
                toItem.setStatus(FriendShipStatus.FRIEND_STATUS_NORMAL.getCode());
                int update = friendShipMapper.update(toItem, toLambdaQueryWrapper);
                if (update != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
            }
        }
        List<FriendShipEntity> friendShipEntities = new ArrayList<>();
        friendShipEntities.add(fromItem);
        friendShipEntities.add(toItem);
        handleAddFriendShipResp.setFriendShipEntities(friendShipEntities);
        return ResponseVO.successResponse(handleAddFriendShipResp);
    }

    @Override
    public ResponseVO<UpdateFriendShipResp> updateFriendShip(UpdateFriendShipReq req) {
        GetUserSequenceReq getUserSequenceReq = new GetUserSequenceReq();
        getUserSequenceReq.setAppId(req.getAppId());
        getUserSequenceReq.setUserId(req.getFromId());
        ResponseVO<GetUserSequenceResp> fromItem = userService.getUserSequence(getUserSequenceReq);
        if (!fromItem.isOk()) {
            return ResponseVO.errorResponse(fromItem.getCode(), fromItem.getMsg());
        }
        getUserSequenceReq.setUserId(req.getToItem().getToId());
        ResponseVO<GetUserSequenceResp> toItem = userService.getUserSequence(getUserSequenceReq);
        if (!toItem.isOk()) {
            return ResponseVO.errorResponse(toItem.getCode(), toItem.getMsg());
        }

        HandleUpdateFriendShipReq handleUpdateFriendShipReq = new HandleUpdateFriendShipReq();
        BeanUtils.copyProperties(req, handleUpdateFriendShipReq);
        ResponseVO<HandleUpdateFriendShipResp> handleUpdateFriendShipRespResponseVO = this.handleUpdateFriendShip(handleUpdateFriendShipReq);
        if (!handleUpdateFriendShipRespResponseVO.isOk()) {
            return ResponseVO.errorResponse(handleUpdateFriendShipRespResponseVO.getCode(), handleUpdateFriendShipRespResponseVO.getMsg());
        }

        UpdateFriendShipResp updateFriendShipResp = new UpdateFriendShipResp();
        updateFriendShipResp.setFriendShipEntity(handleUpdateFriendShipRespResponseVO.getData().getFriendShipEntity());
        return ResponseVO.successResponse(updateFriendShipResp);
    }

    @Override
    @Transactional
    public ResponseVO<HandleUpdateFriendShipResp> handleUpdateFriendShip(HandleUpdateFriendShipReq req) {
        LambdaUpdateWrapper<FriendShipEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.like(FriendShipEntity::getAppId, req.getAppId())
                .like(FriendShipEntity::getFromId, req.getFromId())
                .like(FriendShipEntity::getToId, req.getToItem().getToId())
                .set(FriendShipEntity::getAddSource, req.getToItem().getAddSource())
                .set(FriendShipEntity::getExtra, req.getToItem().getExtra())
                .set(FriendShipEntity::getRemark, req.getToItem().getRemark());
        int update = friendShipMapper.update(null, lambdaUpdateWrapper);
        if (update == 1) {
            LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.like(FriendShipEntity::getAppId, req.getAppId())
                    .like(FriendShipEntity::getFromId, req.getFromId())
                    .like(FriendShipEntity::getToId, req.getToItem().getToId());
            FriendShipEntity friendShipEntity = friendShipMapper.selectOne(lambdaQueryWrapper);
            HandleUpdateFriendShipResp handleUpdateFriendShipResp = new HandleUpdateFriendShipResp();
            handleUpdateFriendShipResp.setFriendShipEntity(friendShipEntity);
            return ResponseVO.successResponse(handleUpdateFriendShipResp);
        }

        return ResponseVO.errorResponse();
    }

    @Override
    public ResponseVO<DeleteFriendShipResp> deleteFriendShip(DeleteFriendShipReq req) {
        LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(FriendShipEntity::getAppId, req.getAppId())
                .like(FriendShipEntity::getFromId, req.getFromId())
                .like(FriendShipEntity::getToId, req.getToId());
        FriendShipEntity friendShipEntity = friendShipMapper.selectOne(lambdaQueryWrapper);
        if (friendShipEntity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
            if (friendShipEntity.getStatus() != null && friendShipEntity.getStatus() == FriendShipStatus.FRIEND_STATUS_NORMAL.getCode()) {
                friendShipEntity.setStatus(FriendShipStatus.FRIEND_STATUS_DELETE.getCode());
                friendShipMapper.update(friendShipEntity, lambdaQueryWrapper);
            } else {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
        }
        DeleteFriendShipResp deleteFriendShipResp = new DeleteFriendShipResp();
        deleteFriendShipResp.setFriendShipEntity(friendShipEntity);
        return ResponseVO.successResponse(deleteFriendShipResp);
    }

    @Override
    public ResponseVO<DeleteAllFriendShipResp> deleteAllFriendShip(DeleteAllFriendShipReq req) {
        LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(FriendShipEntity::getAppId, req.getAppId())
                .like(FriendShipEntity::getFromId, req.getFromId())
                .like(FriendShipEntity::getStatus, FriendShipStatus.FRIEND_STATUS_NORMAL.getCode());
        List<FriendShipEntity> friendShipEntities = friendShipMapper.selectList(lambdaQueryWrapper);
        if (friendShipEntities.size() == 0) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
        }
        friendShipEntities.forEach(item -> item.setStatus(FriendShipStatus.FRIEND_STATUS_DELETE.getCode()));
        FriendShipEntity update = new FriendShipEntity();
        update.setStatus(FriendShipStatus.FRIEND_STATUS_DELETE.getCode());
        friendShipMapper.update(update, lambdaQueryWrapper);
        DeleteAllFriendShipResp deleteAllFriendShipResp = new DeleteAllFriendShipResp();
        deleteAllFriendShipResp.setFriendShipEntities(friendShipEntities);
        return ResponseVO.successResponse(deleteAllFriendShipResp);
    }
}
