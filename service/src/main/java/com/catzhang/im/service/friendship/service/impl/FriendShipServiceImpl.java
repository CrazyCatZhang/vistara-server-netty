package com.catzhang.im.service.friendship.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.*;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.friendship.dao.FriendShipEntity;
import com.catzhang.im.service.friendship.dao.FriendShipRequestEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipMapper;
import com.catzhang.im.service.friendship.model.callback.AddFriendAfterCallbackDto;
import com.catzhang.im.service.friendship.model.callback.DeleteFriendAfterCallbackDto;
import com.catzhang.im.service.friendship.model.callback.UpdateFriendAfterCallbackDto;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipRequestService;
import com.catzhang.im.service.friendship.service.FriendShipService;
import com.catzhang.im.service.user.dao.UserDataEntity;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.req.GetUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.model.resp.GetUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import com.catzhang.im.service.utils.CallbackService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author crazycatzhang
 */
@Service
public class FriendShipServiceImpl implements FriendShipService {

    @Autowired
    FriendShipMapper friendShipMapper;

    @Autowired
    UserService userService;

    @Autowired
    FriendShipRequestService friendShipRequestService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Override
    public ResponseVO<ImportFriendShipResp> importFriendShip(ImportFriendShipReq req) {
        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setUserId(req.getFromId());
        getSingleUserInfoReq.setAppId(req.getAppId());
        ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!singleUserInfo.isOk()) {
            return ResponseVO.errorResponse(singleUserInfo.getCode(), singleUserInfo.getMsg());
        }

        List<String> userIds = new ArrayList<>();
        req.getFriendItem().forEach(item -> {
            userIds.add(item.getToId());
        });
        GetUserInfoReq getUserInfoReq = new GetUserInfoReq();
        getUserInfoReq.setUserIds(userIds);
        getUserInfoReq.setAppId(req.getAppId());
        ResponseVO<GetUserInfoResp> userInfo = userService.getUserInfo(getUserInfoReq);
        List<String> isNotExistUser = userInfo.getData().getFailUser();


        ImportFriendShipResp importFriendShipResp = new ImportFriendShipResp();
        List<String> successIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();
        req.getFriendItem().forEach(item -> {
            if (isNotExistUser.contains(item.getToId())) {
                errorIds.add(item.getToId());
                return;
            }
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
        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setUserId(req.getFromId());
        getSingleUserInfoReq.setAppId(req.getAppId());
        ResponseVO<GetSingleUserInfoResp> fromUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!fromUserInfo.isOk()) {
            return ResponseVO.errorResponse(fromUserInfo.getCode(), fromUserInfo.getMsg());
        }

        getSingleUserInfoReq.setUserId(req.getToItem().getToId());
        ResponseVO<GetSingleUserInfoResp> toUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!toUserInfo.isOk()) {
            return ResponseVO.errorResponse(toUserInfo.getCode(), toUserInfo.getMsg());
        }

        //TODO: 添加好友之前回调
        if (appConfig.isAddFriendBeforeCallback()) {
            ResponseVO callbackResp = callbackService
                    .beforeCallback(req.getAppId(),
                            Constants.CallbackCommand.ADDFRIENDBEFORE
                            , JSONObject.toJSONString(req));
            if (!callbackResp.isOk()) {
                return callbackResp;
            }
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
            //TODO 插入一条好友申请消息
            LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                    .eq(FriendShipEntity::getFromId, req.getFromId())
                    .eq(FriendShipEntity::getToId, req.getToItem().getToId());
            FriendShipEntity fromItem = friendShipMapper.selectOne(lambdaQueryWrapper);
            if (fromItem == null || fromItem.getStatus() != FriendShipStatus.FRIEND_STATUS_NORMAL.getCode()) {
                AddFriendShipRequestReq addFriendShipRequestReq = new AddFriendShipRequestReq();
                addFriendShipRequestReq.setAppId(req.getAppId());
                addFriendShipRequestReq.setFromId(req.getFromId());
                addFriendShipRequestReq.setToItem(req.getToItem());
                ResponseVO<AddFriendShipRequestResp> addFriendShipRequestRespResponseVO = friendShipRequestService.addFriendShipRequest(addFriendShipRequestReq);
                if (!addFriendShipRequestRespResponseVO.isOk()) {
                    return ResponseVO.errorResponse(addFriendShipRequestRespResponseVO.getCode(), addFriendShipRequestRespResponseVO.getMsg());
                }
                return ResponseVO.successResponse(addFriendShipRequestRespResponseVO.getData());
            } else {
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }
        }
    }

    @Override
    @Transactional
    public ResponseVO<HandleAddFriendShipResp> handleAddFriendShip(HandleAddFriendShipReq req) {
        HandleAddFriendShipResp handleAddFriendShipResp = new HandleAddFriendShipResp();
        LambdaQueryWrapper<FriendShipEntity> fromLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fromLambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getFromId())
                .eq(FriendShipEntity::getToId, req.getToItem().getToId());
        FriendShipEntity fromItem = friendShipMapper.selectOne(fromLambdaQueryWrapper);
        if (fromItem == null) {
            fromItem = new FriendShipEntity();
            fromItem.setAppId(req.getAppId());
            fromItem.setFromId(req.getFromId());
            BeanUtils.copyProperties(req.getToItem(), fromItem);
            fromItem.setStatus(FriendShipStatus.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setBlack(FriendShipStatus.BLACK_STATUS_NORMAL.getCode());
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

        AtomicBoolean isApprovedFriendRequest = new AtomicBoolean(false);
        GetFriendShipRequestReq getFriendShipRequestReq = new GetFriendShipRequestReq();
        getFriendShipRequestReq.setAppId(req.getAppId());
        getFriendShipRequestReq.setUserId(req.getToItem().getToId());
        ResponseVO<GetFriendShipRequestResp> friendShipRequest = friendShipRequestService.getFriendShipRequest(getFriendShipRequestReq);
        List<FriendShipRequestEntity> friendShipRequestEntityList = friendShipRequest.getData().getFriendShipRequestEntityList();
        friendShipRequestEntityList.forEach(item -> {
            if (item.getFromId().equals(req.getFromId()) && item.getApproveStatus() == ApproveFriendRequestStatus.AGREE.getCode()) {
                isApprovedFriendRequest.set(true);
            }
        });

        LambdaQueryWrapper<FriendShipEntity> toLambdaQueryWrapper = new LambdaQueryWrapper<>();
        toLambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getToItem().getToId())
                .eq(FriendShipEntity::getToId, req.getFromId());
        FriendShipEntity toItem = friendShipMapper.selectOne(toLambdaQueryWrapper);
        if (toItem == null) {
            toItem = new FriendShipEntity();
            toItem.setAppId(req.getAppId());
            toItem.setFromId(req.getToItem().getToId());
            toItem.setToId(req.getFromId());
            if (isApprovedFriendRequest.get()) {
                toItem.setStatus(FriendShipStatus.FRIEND_STATUS_NORMAL.getCode());
            } else {
                toItem.setStatus(FriendShipStatus.FRIEND_STATUS_NO_FRIEND.getCode());
            }

            toItem.setBlack(FriendShipStatus.BLACK_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
            int inset = friendShipMapper.insert(toItem);
            if (inset != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        } else {
            if (isApprovedFriendRequest.get() && toItem.getStatus() != FriendShipStatus.FRIEND_STATUS_NORMAL.getCode()) {
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

        //TODO: 添加好友之后回调
        if (appConfig.isAddFriendAfterCallback()) {
            AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
            callbackDto.setFromId(req.getFromId());
            callbackDto.setToItem(req.getToItem());
            callbackService.afterCallback(req.getAppId(),
                    Constants.CallbackCommand.ADDFRIENDAFTER, JSONObject
                            .toJSONString(callbackDto));
        }

        return ResponseVO.successResponse(handleAddFriendShipResp);
    }

    @Override
    public ResponseVO<UpdateFriendShipResp> updateFriendShip(UpdateFriendShipReq req) {
        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setAppId(req.getAppId());
        getSingleUserInfoReq.setUserId(req.getFromId());
        ResponseVO<GetSingleUserInfoResp> fromItem = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!fromItem.isOk()) {
            return ResponseVO.errorResponse(fromItem.getCode(), fromItem.getMsg());
        }
        getSingleUserInfoReq.setUserId(req.getToItem().getToId());
        ResponseVO<GetSingleUserInfoResp> toItem = userService.getSingleUserInfo(getSingleUserInfoReq);
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
        lambdaUpdateWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getFromId())
                .eq(FriendShipEntity::getToId, req.getToItem().getToId())
                .set(FriendShipEntity::getAddSource, req.getToItem().getAddSource())
                .set(FriendShipEntity::getExtra, req.getToItem().getExtra())
                .set(FriendShipEntity::getRemark, req.getToItem().getRemark());
        int update = friendShipMapper.update(null, lambdaUpdateWrapper);
        if (update == 1) {
            LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                    .eq(FriendShipEntity::getFromId, req.getFromId())
                    .eq(FriendShipEntity::getToId, req.getToItem().getToId());
            FriendShipEntity friendShipEntity = friendShipMapper.selectOne(lambdaQueryWrapper);
            HandleUpdateFriendShipResp handleUpdateFriendShipResp = new HandleUpdateFriendShipResp();
            handleUpdateFriendShipResp.setFriendShipEntity(friendShipEntity);

            //TODO: 更新好友信息之后回调
            if (appConfig.isModifyFriendAfterCallback()) {
                UpdateFriendAfterCallbackDto callbackDto = new UpdateFriendAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToItem(req.getToItem());
                callbackService.afterCallback(req.getAppId(),
                        Constants.CallbackCommand.UPDATEFRIENDAFTER, JSONObject
                                .toJSONString(callbackDto));
            }

            return ResponseVO.successResponse(handleUpdateFriendShipResp);
        }

        return ResponseVO.errorResponse();
    }

    @Override
    public ResponseVO<DeleteFriendShipResp> deleteFriendShip(DeleteFriendShipReq req) {
        LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getFromId())
                .eq(FriendShipEntity::getToId, req.getToId());
        FriendShipEntity friendShipEntity = friendShipMapper.selectOne(lambdaQueryWrapper);
        if (friendShipEntity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
            if (friendShipEntity.getStatus() != null && friendShipEntity.getStatus() == FriendShipStatus.FRIEND_STATUS_NORMAL.getCode()) {
                friendShipEntity.setStatus(FriendShipStatus.FRIEND_STATUS_DELETE.getCode());
                friendShipMapper.update(friendShipEntity, lambdaQueryWrapper);

                //TODO: 删除好友之后回调
                if (appConfig.isAddFriendAfterCallback()) {
                    DeleteFriendAfterCallbackDto callbackDto = new DeleteFriendAfterCallbackDto();
                    callbackDto.setFromId(req.getFromId());
                    callbackDto.setToId(req.getToId());
                    callbackService.afterCallback(req.getAppId(),
                            Constants.CallbackCommand.DELETEFRIENDAFTER, JSONObject
                                    .toJSONString(callbackDto));
                }

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
        lambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getFromId())
                .eq(FriendShipEntity::getStatus, FriendShipStatus.FRIEND_STATUS_NORMAL.getCode());
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

    @Override
    public ResponseVO<GetAllFriendShipResp> getAllFriendShip(GetAllFriendShipReq req) {
        LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getFromId());
        List<FriendShipEntity> friendShipEntities = friendShipMapper.selectList(lambdaQueryWrapper);
        if (friendShipEntities.size() == 0) {
            return ResponseVO.errorResponse(FriendShipErrorCode.YOU_HAVE_NOT_FRIEND_SHIP);
        }
        GetAllFriendShipResp getAllFriendShipResp = new GetAllFriendShipResp();
        getAllFriendShipResp.setFriendShipEntities(friendShipEntities);
        return ResponseVO.successResponse(getAllFriendShipResp);
    }

    @Override
    public ResponseVO<GetRelationResp> getRelation(GetRelationReq req) {
        LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getFromId())
                .eq(FriendShipEntity::getToId, req.getToId());
        FriendShipEntity friendShipEntity = friendShipMapper.selectOne(lambdaQueryWrapper);
        if (friendShipEntity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        }
        return ResponseVO.successResponse(new GetRelationResp(friendShipEntity));
    }

    @Override
    public ResponseVO<List<VerifyFriendShipResp>> verifyFriendShip(VerifyFriendShipReq req) {

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setUserId(req.getFromId());
        getSingleUserInfoReq.setAppId(req.getAppId());
        ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!singleUserInfo.isOk()) {
            return ResponseVO.errorResponse(singleUserInfo.getCode(), req.getFromId() + singleUserInfo.getMsg());
        }

        Map<String, Integer> result = req.getToIds().stream().collect(Collectors.toMap(Function.identity(), s -> 0));

        List<VerifyFriendShipResp> resp;

        if (req.getCheckType() == VerifyFriendshipType.SINGLE.getType()) {
            resp = friendShipMapper.verifyFriendShip(req);
            resp.forEach(item -> {
                item.setMessage(
                        item.getStatus() == VerifyFriendship.UNIDIRECTIONAL_VERIFICATION_RESULT_IS_FRIEND.getStatus() ?
                                VerifyFriendship.UNIDIRECTIONAL_VERIFICATION_RESULT_IS_FRIEND.getMessage() :
                                VerifyFriendship.UNIDIRECTIONAL_VERIFICATION_RESULT_IS_NO_RELATIONSHIP.getMessage()
                );
            });
        } else {
            resp = friendShipMapper.verifyBidirectionalFriendShip(req);
            resp.forEach(item -> {
                if (item.getStatus() == 1) {
                    item.setMessage(VerifyFriendship.BIDIRECTIONAL_VERIFICATION_RESULT_IS_FRIEND.getMessage());
                } else if (item.getStatus() == 2) {
                    item.setMessage(VerifyFriendship.BIDIRECTIONAL_VERIFICATION_RESULT_IS_A_ADDS_B.getMessage());
                } else if (item.getStatus() == 3) {
                    item.setMessage(VerifyFriendship.BIDIRECTIONAL_VERIFICATION_RESULT_IS_B_ADDS_A.getMessage());
                } else {
                    item.setMessage(VerifyFriendship.BIDIRECTIONAL_VERIFICATION_RESULT_IS_NO_RELATIONSHIP.getMessage());
                }
            });
        }

        Map<String, Integer> collect = resp.stream().collect(Collectors.toMap(VerifyFriendShipResp::getToId, VerifyFriendShipResp::getStatus));
        result.keySet().forEach(toId -> {
            if (!collect.containsKey(toId)) {
                getSingleUserInfoReq.setUserId(toId);
                ResponseVO<GetSingleUserInfoResp> toIdInfo = userService.getSingleUserInfo(getSingleUserInfoReq);

                VerifyFriendShipResp verifyFriendShipResp = new VerifyFriendShipResp();
                verifyFriendShipResp.setFromId(req.getFromId());
                verifyFriendShipResp.setToId(toId);
                if (!toIdInfo.isOk()) {
                    verifyFriendShipResp.setStatus(-1);
                    verifyFriendShipResp.setMessage(toId + toIdInfo.getMsg());
                } else {
                    verifyFriendShipResp.setStatus(0);
                    verifyFriendShipResp.setMessage(VerifyFriendship.UNIDIRECTIONAL_VERIFICATION_RESULT_IS_NO_RELATIONSHIP.getMessage());
                }
                resp.add(verifyFriendShipResp);
            }
        });

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<AddFriendShipBlackResp> blackFriendShip(AddFriendShipBlackReq req) {
        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setAppId(req.getAppId());
        getSingleUserInfoReq.setUserId(req.getFromId());
        ResponseVO<GetSingleUserInfoResp> fromInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!fromInfo.isOk()) {
            return ResponseVO.errorResponse(fromInfo.getCode(), fromInfo.getMsg());
        }
        getSingleUserInfoReq.setUserId(req.getToId());
        ResponseVO<GetSingleUserInfoResp> toInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!toInfo.isOk()) {
            return ResponseVO.errorResponse(toInfo.getCode(), toInfo.getMsg());
        }

        LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getFromId())
                .eq(FriendShipEntity::getToId, req.getToId());
        FriendShipEntity fromItem = friendShipMapper.selectOne(lambdaQueryWrapper);
        if (fromItem == null) {
            fromItem = new FriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setAppId(req.getAppId());
            fromItem.setStatus(FriendShipStatus.FRIEND_STATUS_NO_FRIEND.getCode());
            fromItem.setBlack(FriendShipStatus.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = friendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        } else {
            if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatus.BLACK_STATUS_BLACKED.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
            } else {
                fromItem.setBlack(FriendShipStatus.BLACK_STATUS_BLACKED.getCode());
                int update = friendShipMapper.update(fromItem, lambdaQueryWrapper);
                if (update != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
            }
        }

        AddFriendShipBlackResp addFriendShipBlackResp = new AddFriendShipBlackResp();
        addFriendShipBlackResp.setBlackShipEntity(fromItem);
        return ResponseVO.successResponse(addFriendShipBlackResp);
    }

    @Override
    public ResponseVO<DeleteFriendShipBlackResp> deleteFriendShipBlack(DeleteFriendShipBlackReq req) {
        LambdaQueryWrapper<FriendShipEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipEntity::getAppId, req.getAppId())
                .eq(FriendShipEntity::getFromId, req.getFromId())
                .eq(FriendShipEntity::getToId, req.getToId());
        FriendShipEntity fromItem = friendShipMapper.selectOne(lambdaQueryWrapper);

        if (fromItem == null) {
            throw new ApplicationException(FriendShipErrorCode.RELATION_SHIP_IS_NOT_EXIST);
        }

        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatus.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }
        fromItem.setBlack(FriendShipStatus.BLACK_STATUS_NORMAL.getCode());
        int update = friendShipMapper.update(fromItem, lambdaQueryWrapper);
        if (update != 1) {
            return ResponseVO.errorResponse();
        }
        return ResponseVO.successResponse(new DeleteFriendShipBlackResp(fromItem));
    }

    @Override
    public ResponseVO<List<VerifyFriendShipResp>> verifyFriendShipBlack(VerifyFriendShipReq req) {

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setUserId(req.getFromId());
        getSingleUserInfoReq.setAppId(req.getAppId());
        ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!singleUserInfo.isOk()) {
            return ResponseVO.errorResponse(singleUserInfo.getCode(), req.getFromId() + singleUserInfo.getMsg());
        }

        Map<String, Integer> result = req.getToIds().stream().collect(Collectors.toMap(Function.identity(), s -> 0));

        List<VerifyFriendShipResp> resp;
        if (req.getCheckType() == VerifyFriendshipType.SINGLE.getType()) {
            resp = friendShipMapper.verifyFriendShipBlack(req);
            resp.forEach(item -> {
                item.setMessage(
                        item.getStatus() == VerifyFriendship.UNIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_NORMAL.getStatus() ?
                                VerifyFriendship.UNIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_NORMAL.getMessage() :
                                VerifyFriendship.UNIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_BLACKED.getMessage()
                );
            });
        } else {
            resp = friendShipMapper.verifyBidirectionalFriendShipBlack(req);
            resp.forEach(item -> {
                if (item.getStatus() == 1) {
                    item.setMessage(VerifyFriendship.BIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_NORMAL.getMessage());
                } else if (item.getStatus() == 2) {
                    item.setMessage(VerifyFriendship.BIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_B_BLACKED_A.getMessage());
                } else if (item.getStatus() == 3) {
                    item.setMessage(VerifyFriendship.BIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_A_BLACKED_B.getMessage());
                } else {
                    item.setMessage(VerifyFriendship.BIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_BLACKED.getMessage());
                }
            });
        }

        Map<String, Integer> collect = resp.stream().collect(Collectors.toMap(VerifyFriendShipResp::getToId, VerifyFriendShipResp::getStatus));
        result.keySet().forEach(toId -> {
            if (!collect.containsKey(toId)) {

                getSingleUserInfoReq.setUserId(toId);
                ResponseVO<GetSingleUserInfoResp> toIdInfo = userService.getSingleUserInfo(getSingleUserInfoReq);

                VerifyFriendShipResp verifyFriendShipResp = new VerifyFriendShipResp();
                verifyFriendShipResp.setFromId(req.getFromId());
                verifyFriendShipResp.setToId(toId);
                if (!toIdInfo.isOk()) {
                    verifyFriendShipResp.setStatus(-1);
                    verifyFriendShipResp.setMessage(toId + toIdInfo.getMsg());
                } else {
                    verifyFriendShipResp.setStatus(0);
                    verifyFriendShipResp.setMessage(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND.getError());
                }
                resp.add(verifyFriendShipResp);
            }
        });
        return ResponseVO.successResponse(resp);
    }
}
