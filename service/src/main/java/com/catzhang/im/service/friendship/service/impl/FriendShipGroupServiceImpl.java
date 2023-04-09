package com.catzhang.im.service.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.DelFlagEnum;
import com.catzhang.im.common.enums.FriendShipErrorCode;
import com.catzhang.im.service.friendship.dao.FriendShipGroupEntity;
import com.catzhang.im.service.friendship.dao.FriendShipGroupMemberEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipGroupMapper;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipGroupMemberService;
import com.catzhang.im.service.friendship.service.FriendShipGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author crazycatzhang
 */
@Service
public class FriendShipGroupServiceImpl implements FriendShipGroupService {

    @Autowired
    FriendShipGroupMapper friendShipGroupMapper;

    @Autowired
    FriendShipGroupMemberService friendShipGroupMemberService;

    @Override
    public ResponseVO<AddFriendShipGroupResp> addFriendShipGroup(AddFriendShipGroupReq req) {
        System.out.println(req);
        LambdaQueryWrapper<FriendShipGroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(FriendShipGroupEntity::getAppId, req.getAppId())
                .like(FriendShipGroupEntity::getGroupName, req.getGroupName())
                .like(FriendShipGroupEntity::getFromId, req.getFromId());

        FriendShipGroupEntity friendShipGroupEntity = friendShipGroupMapper.selectOne(lambdaQueryWrapper);
        if (friendShipGroupEntity != null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }

        FriendShipGroupEntity friendShipGroup = new FriendShipGroupEntity();
        friendShipGroup.setAppId(req.getAppId());
        friendShipGroup.setGroupName(req.getGroupName());
        friendShipGroup.setFromId(req.getFromId());
        friendShipGroup.setDelFlag(DelFlagEnum.NORMAL.getCode());
        friendShipGroup.setCreateTime(System.currentTimeMillis());
        System.out.println(friendShipGroup);
        try {
            int insert = friendShipGroupMapper.insert(friendShipGroup);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_CREATE_ERROR);
            }
            if (CollectionUtil.isNotEmpty(req.getToIds())) {
                System.out.println("11111111111111111111");
                AddFriendShipGroupMemberReq addFriendShipGroupMemberReq = new AddFriendShipGroupMemberReq();
                addFriendShipGroupMemberReq.setAppId(req.getAppId());
                addFriendShipGroupMemberReq.setGroupName(req.getGroupName());
                addFriendShipGroupMemberReq.setFromId(req.getFromId());
                addFriendShipGroupMemberReq.setToIds(req.getToIds());
                ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMemberRespResponseVO = friendShipGroupMemberService.addFriendShipGroupMember(addFriendShipGroupMemberReq);
                if (!addFriendShipGroupMemberRespResponseVO.isOk()) {
                    return ResponseVO.errorResponse(addFriendShipGroupMemberRespResponseVO.getCode(), addFriendShipGroupMemberRespResponseVO.getMsg());
                }
                return ResponseVO.successResponse(new AddFriendShipGroupResp(friendShipGroup));
            }
        } catch (DuplicateKeyException e) {
            e.getStackTrace();
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }

        return ResponseVO.successResponse(new AddFriendShipGroupResp(friendShipGroup));
    }

    @Override
    public ResponseVO<GetFriendShipGroupResp> getFriendShipGroup(GetFriendShipGroupReq req) {
        LambdaQueryWrapper<FriendShipGroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(FriendShipGroupEntity::getAppId, req.getAppId())
                .like(FriendShipGroupEntity::getFromId, req.getFromId())
                .like(FriendShipGroupEntity::getGroupName, req.getGroupName());
        FriendShipGroupEntity friendShipGroupEntity = friendShipGroupMapper.selectOne(lambdaQueryWrapper);
        if (friendShipGroupEntity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(new GetFriendShipGroupResp(friendShipGroupEntity));
    }

    @Override
    public ResponseVO<DeleteFriendShipGroupResp> deleteFriendShipGroup(DeleteFriendShipGroupReq req) {
        Map<String, List<String>> successGroups = new HashMap<>();
        Map<String, String> failureGroups = new HashMap<>();

        List<String> toIds = new ArrayList<>();
        for (String groupName : req.getGroupNames()) {
            toIds.clear();
            LambdaQueryWrapper<FriendShipGroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.like(FriendShipGroupEntity::getAppId, req.getAppId())
                    .like(FriendShipGroupEntity::getGroupName, groupName)
                    .like(FriendShipGroupEntity::getFromId, req.getFromId());
            FriendShipGroupEntity friendShipGroupEntity = friendShipGroupMapper.selectOne(lambdaQueryWrapper);
            if (friendShipGroupEntity != null) {
                int delete = friendShipGroupMapper.delete(lambdaQueryWrapper);
                if (delete != 1) {
                    failureGroups.put(groupName, FriendShipErrorCode.GROUP_DELETION_FAILED.getError());
                }
                ClearFriendShipGroupMemberReq clearFriendShipGroupMemberReq = new ClearFriendShipGroupMemberReq();
                clearFriendShipGroupMemberReq.setGroupId(friendShipGroupEntity.getGroupId());
                ResponseVO<ClearFriendShipGroupMemberResp> clearFriendShipGroupMemberRespResponseVO = friendShipGroupMemberService.clearFriendShipGroupMember(clearFriendShipGroupMemberReq);
                if (!clearFriendShipGroupMemberRespResponseVO.isOk()) {
                    failureGroups.put(groupName, clearFriendShipGroupMemberRespResponseVO.getMsg());
                }
                List<FriendShipGroupMemberEntity> friendShipGroupMemberEntityList = clearFriendShipGroupMemberRespResponseVO.getData().getFriendShipGroupMemberEntityList();
                friendShipGroupMemberEntityList.forEach(item -> {
                    toIds.add(item.getToId());
                });
                successGroups.put(groupName, toIds);
            } else {
                failureGroups.put(groupName, FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_NOT_EXIST.getError());
            }
        }
        return ResponseVO.successResponse(new DeleteFriendShipGroupResp(successGroups, failureGroups));
    }
}
