package com.catzhang.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.ApproveFriendRequestStatus;
import com.catzhang.im.common.enums.FriendShipErrorCode;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.friendship.dao.FriendShipRequestEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipRequestMapper;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipRequestResp;
import com.catzhang.im.service.friendship.model.resp.ApproveFriendRequestResp;
import com.catzhang.im.service.friendship.model.resp.HandleAddFriendShipResp;
import com.catzhang.im.service.friendship.model.resp.ReadFriendShipRequestResp;
import com.catzhang.im.service.friendship.service.FriendShipRequestService;
import com.catzhang.im.service.friendship.service.FriendShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class FriendshipRequestServiceImpl implements FriendShipRequestService {

    @Autowired
    FriendShipRequestMapper friendShipRequestMapper;

    @Autowired
    FriendShipService friendShipService;

    @Override
    public ResponseVO<AddFriendShipRequestResp> addFriendShipRequest(AddFriendShipRequestReq req) {
        FriendDto toItem = req.getToItem();
        LambdaQueryWrapper<FriendShipRequestEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(FriendShipRequestEntity::getAppId, req.getAppId())
                .like(FriendShipRequestEntity::getFromId, req.getFromId())
                .like(FriendShipRequestEntity::getToId, toItem.getToId());
        FriendShipRequestEntity friendShipRequest = friendShipRequestMapper.selectOne(lambdaQueryWrapper);
        if (friendShipRequest == null) {
            friendShipRequest = new FriendShipRequestEntity();
            friendShipRequest.setAppId(req.getAppId());
            friendShipRequest.setFromId(req.getFromId());
            friendShipRequest.setToId(toItem.getToId());
            friendShipRequest.setRemark(toItem.getRemark());
            friendShipRequest.setAddSource(toItem.getAddSource());
            friendShipRequest.setAddWording(toItem.getAddWording());
            friendShipRequest.setReadStatus(0);
            friendShipRequest.setApproveStatus(0);
            friendShipRequest.setCreateTime(System.currentTimeMillis());
            int insert = friendShipRequestMapper.insert(friendShipRequest);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_REQUEST_IS_FAILED);
            }
        } else {
            if (StringUtils.isNotBlank(toItem.getAddSource())) {
                friendShipRequest.setAddWording(toItem.getAddWording());
            }
            if (StringUtils.isNotBlank(toItem.getRemark())) {
                friendShipRequest.setRemark(toItem.getRemark());
            }
            if (StringUtils.isNotBlank(toItem.getAddWording())) {
                friendShipRequest.setAddWording(toItem.getAddWording());
            }
            friendShipRequest.setReadStatus(0);
            friendShipRequest.setApproveStatus(0);
            friendShipRequest.setUpdateTime(System.currentTimeMillis());
            int update = friendShipRequestMapper.update(friendShipRequest, lambdaQueryWrapper);
            if (update != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_REQUEST_IS_FAILED);
            }
        }
        return ResponseVO.successResponse(new AddFriendShipRequestResp(friendShipRequest));
    }

    @Override
    public ResponseVO<ApproveFriendRequestResp> approveFriendRequest(ApproveFriendRequestReq req) {
        FriendShipRequestEntity friendShipRequestEntity = friendShipRequestMapper.selectById(req.getId());
        if (friendShipRequestEntity == null) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }
        if (!req.getOperator().equals(friendShipRequestEntity.getToId())) {
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }
        friendShipRequestEntity.setApproveStatus(req.getStatus());
        friendShipRequestEntity.setUpdateTime(System.currentTimeMillis());
        int update = friendShipRequestMapper.updateById(friendShipRequestEntity);
        if (update != 1) {
            return ResponseVO.errorResponse();
        }
        if (req.getStatus() == ApproveFriendRequestStatus.AGREE.getCode()) {
            FriendDto friendDto = new FriendDto();
            friendDto.setRemark(friendShipRequestEntity.getRemark());
            friendDto.setAddWording(friendShipRequestEntity.getAddWording());
            friendDto.setAddSource(friendShipRequestEntity.getAddSource());
            friendDto.setToId(friendShipRequestEntity.getToId());
            HandleAddFriendShipReq handleAddFriendShipReq = new HandleAddFriendShipReq();
            handleAddFriendShipReq.setAppId(req.getAppId());
            handleAddFriendShipReq.setFromId(friendShipRequestEntity.getFromId());
            handleAddFriendShipReq.setToItem(friendDto);
            ResponseVO<HandleAddFriendShipResp> handleAddFriendShipRespResponseVO = friendShipService.handleAddFriendShip(handleAddFriendShipReq);
            if (!handleAddFriendShipRespResponseVO.isOk() && handleAddFriendShipRespResponseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()) {
                return ResponseVO.errorResponse(handleAddFriendShipRespResponseVO.getCode(), handleAddFriendShipRespResponseVO.getMsg());
            }
        }
        return ResponseVO.successResponse(new ApproveFriendRequestResp(friendShipRequestEntity));
    }

    @Override
    public ResponseVO<ReadFriendShipRequestResp> readFriendShipRequest(ReadFriendShipRequestReq req) {
        LambdaUpdateWrapper<FriendShipRequestEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.like(FriendShipRequestEntity::getAppId, req.getAppId())
                .like(FriendShipRequestEntity::getToId, req.getUserId())
                .set(FriendShipRequestEntity::getReadStatus, 1);
        friendShipRequestMapper.update(null, lambdaUpdateWrapper);
        LambdaQueryWrapper<FriendShipRequestEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(FriendShipRequestEntity::getAppId, req.getAppId())
                .like(FriendShipRequestEntity::getToId, req.getUserId());
        List<FriendShipRequestEntity> friendShipRequestEntities = friendShipRequestMapper.selectList(lambdaQueryWrapper);
        friendShipRequestEntities.forEach(System.out::println);
        return ResponseVO.successResponse(new ReadFriendShipRequestResp(friendShipRequestEntities));
    }
}
