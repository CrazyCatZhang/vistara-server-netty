package com.catzhang.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.catzhang.im.codec.pack.friendship.AddFriendRequestPack;
import com.catzhang.im.codec.pack.friendship.ApproveFriendRequestPack;
import com.catzhang.im.codec.pack.friendship.ReadAllFriendRequestPack;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ApproveFriendRequestStatus;
import com.catzhang.im.common.enums.FriendShipErrorCode;
import com.catzhang.im.common.enums.command.FriendshipEventCommand;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.friendship.dao.FriendShipRequestEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipRequestMapper;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipRequestService;
import com.catzhang.im.service.friendship.service.FriendShipService;
import com.catzhang.im.service.sequence.RedisSequence;
import com.catzhang.im.service.utils.MessageProducer;
import com.catzhang.im.service.utils.WriteUserSequence;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    RedisSequence redisSequence;

    @Autowired
    WriteUserSequence writeUserSequence;

    @Autowired
    MessageProducer messageProducer;


    @Override
    public ResponseVO<AddFriendShipRequestResp> addFriendShipRequest(AddFriendShipRequestReq req) {
        FriendDto toItem = req.getToItem();
        LambdaQueryWrapper<FriendShipRequestEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipRequestEntity::getAppId, req.getAppId())
                .eq(FriendShipRequestEntity::getFromId, req.getFromId())
                .eq(FriendShipRequestEntity::getToId, toItem.getToId());
        FriendShipRequestEntity friendShipRequest = friendShipRequestMapper.selectOne(lambdaQueryWrapper);

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.FRIENDSHIPREQUEST);

        if (friendShipRequest == null) {
            friendShipRequest = new FriendShipRequestEntity();
            friendShipRequest.setSequence(sequence);
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
            friendShipRequest.setSequence(sequence);
            int update = friendShipRequestMapper.update(friendShipRequest, lambdaQueryWrapper);
            if (update != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_REQUEST_IS_FAILED);
            }
        }

        writeUserSequence.writeUserSequence(req.getAppId(), req.getToItem().getToId(), Constants.SequenceConstants.FRIENDSHIPREQUEST, sequence);


        //TODO: 好友添加申请TCP通知
        AddFriendRequestPack addFriendRequestPack = new AddFriendRequestPack();
        BeanUtils.copyProperties(friendShipRequest, addFriendRequestPack);
        messageProducer.sendToUser(req.getToItem().getToId(),
                req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_REQUEST,
                addFriendRequestPack, req.getAppId());

        return ResponseVO.successResponse(new AddFriendShipRequestResp(friendShipRequest));
    }

    @Override
    @Transactional
    public ResponseVO<ApproveFriendRequestResp> approveFriendRequest(ApproveFriendRequestReq req) {
        FriendShipRequestEntity friendShipRequestEntity = friendShipRequestMapper.selectById(req.getId());
        if (friendShipRequestEntity == null) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }
        if (!req.getOperator().equals(friendShipRequestEntity.getToId())) {
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }

        if (friendShipRequestEntity.getApproveStatus() == ApproveFriendRequestStatus.AGREE.getCode() || friendShipRequestEntity.getApproveStatus() == ApproveFriendRequestStatus.REJECT.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_APPROVED);
        }

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.FRIENDSHIPREQUEST);

        friendShipRequestEntity.setApproveStatus(req.getStatus());
        friendShipRequestEntity.setUpdateTime(System.currentTimeMillis());
        friendShipRequestEntity.setSequence(sequence);
        int update = friendShipRequestMapper.updateById(friendShipRequestEntity);
        if (update != 1) {
            return ResponseVO.errorResponse();
        }

        writeUserSequence.writeUserSequence(req.getAppId(), req.getOperator(),
                Constants.SequenceConstants.FRIENDSHIPREQUEST, sequence);

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

        //TODO: 审批好友申请的TCP通知
        ApproveFriendRequestPack approveFriendRequestPack = new ApproveFriendRequestPack();
        approveFriendRequestPack.setId(req.getId());
        approveFriendRequestPack.setSequence(sequence);
        approveFriendRequestPack.setStatus(req.getStatus());
        messageProducer.sendToUser(friendShipRequestEntity.getToId(), req.getClientType(), req.getImei(), FriendshipEventCommand
                .FRIEND_REQUEST_APPROVE, approveFriendRequestPack, req.getAppId());

        return ResponseVO.successResponse(new ApproveFriendRequestResp(friendShipRequestEntity));
    }

    @Override
    public ResponseVO<ReadFriendShipRequestResp> readFriendShipRequest(ReadFriendShipRequestReq req) {

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.FRIENDSHIPREQUEST);

        LambdaUpdateWrapper<FriendShipRequestEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(FriendShipRequestEntity::getAppId, req.getAppId())
                .eq(FriendShipRequestEntity::getToId, req.getUserId())
                .set(FriendShipRequestEntity::getReadStatus, 1)
                .set(FriendShipRequestEntity::getSequence, sequence);
        friendShipRequestMapper.update(null, lambdaUpdateWrapper);
        writeUserSequence.writeUserSequence(req.getAppId(), req.getOperator(),
                Constants.SequenceConstants.FRIENDSHIPREQUEST, sequence);

        GetFriendShipRequestReq getFriendShipRequestReq = new GetFriendShipRequestReq();
        BeanUtils.copyProperties(req, getFriendShipRequestReq);
        ResponseVO<GetFriendShipRequestResp> friendShipRequest = this.getFriendShipRequest(getFriendShipRequestReq);
        if (!friendShipRequest.isOk()) {
            return ResponseVO.errorResponse(friendShipRequest.getCode(), friendShipRequest.getMsg());
        }

        //TODO: 已读好友申请TCP通知
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getUserId());
        readAllFriendRequestPack.setSequence(sequence);
        messageProducer.sendToUser(req.getUserId(), req.getClientType(), req.getImei(), FriendshipEventCommand
                .FRIEND_REQUEST_READ, readAllFriendRequestPack, req.getAppId());

        return ResponseVO.successResponse(new ReadFriendShipRequestResp(friendShipRequest.getData().getFriendShipRequestEntityList()));
    }

    @Override
    public ResponseVO<GetFriendShipRequestResp> getFriendShipRequest(GetFriendShipRequestReq req) {
        LambdaQueryWrapper<FriendShipRequestEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipRequestEntity::getAppId, req.getAppId())
                .eq(FriendShipRequestEntity::getToId, req.getUserId());
        List<FriendShipRequestEntity> friendShipRequestEntities = friendShipRequestMapper.selectList(lambdaQueryWrapper);
        if (friendShipRequestEntities.size() == 0) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(new GetFriendShipRequestResp(friendShipRequestEntities));
    }
}
