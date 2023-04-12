package com.catzhang.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.FriendShipErrorCode;
import com.catzhang.im.service.friendship.dao.FriendShipGroupMemberEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipGroupMemberMapper;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipGroupMemberService;
import com.catzhang.im.service.friendship.service.FriendShipGroupService;
import com.catzhang.im.service.friendship.service.FriendShipService;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class FriendShipGroupMemberServiceImpl implements FriendShipGroupMemberService {

    @Autowired
    FriendShipGroupMemberMapper friendShipGroupMemberMapper;

    @Autowired
    FriendShipGroupService friendShipGroupService;

    @Autowired
    FriendShipService friendShipService;

    @Autowired
    UserService userService;

    @Override
    @Transactional
    public ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMember(AddFriendShipGroupMemberReq req) {
        GetFriendShipGroupReq getFriendShipGroupReq = new GetFriendShipGroupReq();
        getFriendShipGroupReq.setAppId(req.getAppId());
        getFriendShipGroupReq.setGroupName(req.getGroupName());
        getFriendShipGroupReq.setFromId(req.getFromId());
        ResponseVO<GetFriendShipGroupResp> friendShipGroup = friendShipGroupService.getFriendShipGroup(getFriendShipGroupReq);
        if (!friendShipGroup.isOk()) {
            return ResponseVO.errorResponse(friendShipGroup.getCode(), friendShipGroup.getMsg());
        }

        VerifyFriendShipReq verifyFriendShipReq = new VerifyFriendShipReq();
        verifyFriendShipReq.setCheckType(2);
        verifyFriendShipReq.setAppId(req.getAppId());
        verifyFriendShipReq.setFromId(req.getFromId());
        verifyFriendShipReq.setToIds(req.getToIds());
        ResponseVO<List<VerifyFriendShipResp>> listResponseVO = friendShipService.verifyFriendShip(verifyFriendShipReq);

        List<VerifyFriendShipResp> data = listResponseVO.getData();
        List<String> isNotFriendIds = new ArrayList<>();
        data.forEach(verifyFriendShipResp -> {
            if (verifyFriendShipResp.getStatus() != 1) {
                isNotFriendIds.add(verifyFriendShipResp.getToId());
            }
        });

        List<String> successIds = new ArrayList<>();
        List<String> failureIds = new ArrayList<>();

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setAppId(req.getAppId());
        HandleAddFriendShipGroupMemberReq handleAddFriendShipGroupMemberReq = new HandleAddFriendShipGroupMemberReq();
        handleAddFriendShipGroupMemberReq.setGroupId(friendShipGroup.getData().getFriendShipGroupEntity().getGroupId());

        for (String toId :
                req.getToIds()) {
            if (isNotFriendIds.contains(toId)) {
                continue;
            }
            getSingleUserInfoReq.setUserId(toId);
            handleAddFriendShipGroupMemberReq.setToId(toId);
            ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
            if (singleUserInfo.isOk()) {
                ResponseVO<HandleAddFriendShipGroupMemberResp> handleAddFriendShipGroupMemberRespResponseVO = this.handleAddFriendShipGroupMember(handleAddFriendShipGroupMemberReq);
                if (!handleAddFriendShipGroupMemberRespResponseVO.isOk()) {
                    failureIds.add(toId);
                    continue;
                }
                if (handleAddFriendShipGroupMemberRespResponseVO.getData().getResult() == 1) {
                    successIds.add(toId);
                }
            } else {
                return ResponseVO.errorResponse(singleUserInfo.getCode(), toId + singleUserInfo.getMsg());
            }
        }
        return ResponseVO.successResponse(new AddFriendShipGroupMemberResp(successIds, failureIds));
    }

    @Override
    public ResponseVO<HandleAddFriendShipGroupMemberResp> handleAddFriendShipGroupMember(HandleAddFriendShipGroupMemberReq req) {
        FriendShipGroupMemberEntity friendShipGroupMemberEntity = new FriendShipGroupMemberEntity();
        friendShipGroupMemberEntity.setGroupId(req.getGroupId());
        friendShipGroupMemberEntity.setToId(req.getToId());
        try {
            int insert = friendShipGroupMemberMapper.insert(friendShipGroupMemberEntity);
            return ResponseVO.successResponse(new HandleAddFriendShipGroupMemberResp(insert));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.errorResponse();
        }
    }

    @Override
    public ResponseVO<ClearFriendShipGroupMemberResp> clearFriendShipGroupMember(ClearFriendShipGroupMemberReq req) {
        LambdaQueryWrapper<FriendShipGroupMemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipGroupMemberEntity::getGroupId, req.getGroupId());
        List<FriendShipGroupMemberEntity> friendShipGroupMemberEntities = friendShipGroupMemberMapper.selectList(lambdaQueryWrapper);
        if (friendShipGroupMemberEntities.size() == 0) {
            return ResponseVO.errorResponse(FriendShipErrorCode.NO_FRIENDS_IN_THE_GROUP);
        }

        int delete = friendShipGroupMemberMapper.delete(lambdaQueryWrapper);
        if (delete == 0) {
            return ResponseVO.successResponse(FriendShipErrorCode.FAILED_TO_CLEAR_GROUP_FRIENDS);
        }
        return ResponseVO.successResponse(new ClearFriendShipGroupMemberResp(friendShipGroupMemberEntities));
    }

    @Override
    public ResponseVO<DeleteFriendShipGroupMemberResp> deleteFriendShipGroupMember(DeleteFriendShipGroupMemberReq req) {
        GetFriendShipGroupReq getFriendShipGroupReq = new GetFriendShipGroupReq();
        getFriendShipGroupReq.setGroupName(req.getGroupName());
        getFriendShipGroupReq.setAppId(req.getAppId());
        getFriendShipGroupReq.setFromId(req.getFromId());

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setAppId(req.getAppId());
        HandleDeleteFriendShipGroupMemberReq handleDeleteFriendShipGroupMemberReq = new HandleDeleteFriendShipGroupMemberReq();

        ResponseVO<GetFriendShipGroupResp> friendShipGroup = friendShipGroupService.getFriendShipGroup(getFriendShipGroupReq);
        handleDeleteFriendShipGroupMemberReq.setGroupId(friendShipGroup.getData().getFriendShipGroupEntity().getGroupId());

        if (!friendShipGroup.isOk()) {
            return ResponseVO.errorResponse(friendShipGroup.getCode(), friendShipGroup.getMsg());
        }

        List<String> successIds = new ArrayList<>();
        List<String> failureIds = new ArrayList<>();

        req.getToIds().forEach(toId -> {
            getSingleUserInfoReq.setUserId(toId);
            handleDeleteFriendShipGroupMemberReq.setToId(toId);
            ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
            if (singleUserInfo.isOk()) {
                ResponseVO<HandleDeleteFriendShipGroupMemberResp> handleDeleteFriendShipGroupMemberRespResponseVO = this.handleDeleteFriendShipGroupMember(handleDeleteFriendShipGroupMemberReq);
                if (handleDeleteFriendShipGroupMemberRespResponseVO.getData().getResult() == 1) {
                    successIds.add(toId);
                } else {
                    failureIds.add(toId);
                }
            }
        });

        return ResponseVO.successResponse(new DeleteFriendShipGroupMemberResp(successIds, failureIds));
    }

    @Override
    public ResponseVO<HandleDeleteFriendShipGroupMemberResp> handleDeleteFriendShipGroupMember(HandleDeleteFriendShipGroupMemberReq req) {
        LambdaQueryWrapper<FriendShipGroupMemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipGroupMemberEntity::getGroupId, req.getGroupId())
                .eq(FriendShipGroupMemberEntity::getToId, req.getToId());
        int result;
        try {
            result = friendShipGroupMemberMapper.delete(lambdaQueryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            result = 0;
        }
        return ResponseVO.successResponse(new HandleDeleteFriendShipGroupMemberResp(result));
    }

    @Override
    public ResponseVO<GetAllFriendShipGroupMemberResp> getAllFriendShipGroupMember(GetAllFriendShipGroupMemberReq req) {
        LambdaQueryWrapper<FriendShipGroupMemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipGroupMemberEntity::getGroupId, req.getGroupId());
        List<FriendShipGroupMemberEntity> friendShipGroupMemberEntities = friendShipGroupMemberMapper.selectList(lambdaQueryWrapper);
        if (friendShipGroupMemberEntities.size() == 0) {
            return ResponseVO.errorResponse(FriendShipErrorCode.NO_FRIENDS_IN_THE_GROUP);
        } else {
            return ResponseVO.successResponse(new GetAllFriendShipGroupMemberResp(friendShipGroupMemberEntities));
        }
    }
}
