package com.catzhang.im.service.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catzhang.im.codec.pack.friendship.AddFriendGroupPack;
import com.catzhang.im.codec.pack.friendship.DeleteFriendGroupPack;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.DelFlagEnum;
import com.catzhang.im.common.enums.FriendShipErrorCode;
import com.catzhang.im.common.enums.command.FriendshipEventCommand;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.service.friendship.dao.FriendShipGroupEntity;
import com.catzhang.im.service.friendship.dao.FriendShipGroupMemberEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipGroupMapper;
import com.catzhang.im.service.friendship.model.req.*;
import com.catzhang.im.service.friendship.model.resp.*;
import com.catzhang.im.service.friendship.service.FriendShipGroupMemberService;
import com.catzhang.im.service.friendship.service.FriendShipGroupService;
import com.catzhang.im.service.sequence.RedisSequence;
import com.catzhang.im.service.utils.MessageProducer;
import com.catzhang.im.service.utils.WriteUserSequence;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
public class FriendShipGroupServiceImpl implements FriendShipGroupService {

    @Autowired
    FriendShipGroupMapper friendShipGroupMapper;

    @Autowired
    FriendShipGroupMemberService friendShipGroupMemberService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSequence redisSequence;

    @Autowired
    WriteUserSequence writeUserSequence;

    @Override
    @Transactional
    public ResponseVO<AddFriendShipGroupResp> addFriendShipGroup(AddFriendShipGroupReq req) {
        LambdaQueryWrapper<FriendShipGroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipGroupEntity::getAppId, req.getAppId())
                .eq(FriendShipGroupEntity::getGroupName, req.getGroupName())
                .eq(FriendShipGroupEntity::getFromId, req.getFromId());

        FriendShipGroupEntity friendShipGroupEntity = friendShipGroupMapper.selectOne(lambdaQueryWrapper);
        if (friendShipGroupEntity != null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }

        IsDeletedFriendShipGroupReq isDeletedFriendShipGroupReq = new IsDeletedFriendShipGroupReq();
        isDeletedFriendShipGroupReq.setGroupName(req.getGroupName());
        isDeletedFriendShipGroupReq.setAppId(req.getAppId());
        isDeletedFriendShipGroupReq.setFromId(req.getFromId());
        FriendShipGroupEntity deletedGroup = friendShipGroupMapper.isDeletedGroup(isDeletedFriendShipGroupReq);
        FriendShipGroupEntity friendShipGroup;

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.FRIENDSHIPGROUP);

        if (deletedGroup == null) {
            friendShipGroup = new FriendShipGroupEntity();
            friendShipGroup.setAppId(req.getAppId());
            friendShipGroup.setGroupName(req.getGroupName());
            friendShipGroup.setSequence(sequence);
            friendShipGroup.setFromId(req.getFromId());
            friendShipGroup.setDelFlag(DelFlagEnum.NORMAL.getCode());
            friendShipGroup.setCreateTime(System.currentTimeMillis());
            try {
                int insert = friendShipGroupMapper.insert(friendShipGroup);
                if (insert != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_CREATE_ERROR);
                }
            } catch (DuplicateKeyException e) {
                e.getStackTrace();
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
            }
        } else {
            RecoveryFriendShipGroupReq recoveryFriendShipGroupReq = new RecoveryFriendShipGroupReq();
            BeanUtils.copyProperties(isDeletedFriendShipGroupReq, recoveryFriendShipGroupReq);
            recoveryFriendShipGroupReq.setUpdateTime(System.currentTimeMillis());
            friendShipGroupMapper.recovery(recoveryFriendShipGroupReq);
            deletedGroup.setDelFlag(DelFlagEnum.NORMAL.getCode());
            deletedGroup.setSequence(sequence);
            deletedGroup.setUpdateTime(recoveryFriendShipGroupReq.getUpdateTime());
            friendShipGroup = deletedGroup;
        }

        writeUserSequence.writeUserSequence(req.getAppId(), req.getFromId(), Constants.SequenceConstants.FRIENDSHIPGROUP, sequence);

        if (CollectionUtil.isNotEmpty(req.getToIds())) {
            AddFriendShipGroupMemberReq addFriendShipGroupMemberReq = new AddFriendShipGroupMemberReq();
            addFriendShipGroupMemberReq.setAppId(req.getAppId());
            addFriendShipGroupMemberReq.setGroupName(req.getGroupName());
            addFriendShipGroupMemberReq.setFromId(req.getFromId());
            addFriendShipGroupMemberReq.setToIds(req.getToIds());
            ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMemberRespResponseVO = friendShipGroupMemberService.addFriendShipGroupMember(addFriendShipGroupMemberReq);
            if (!addFriendShipGroupMemberRespResponseVO.isOk()) {
                return ResponseVO.errorResponse(addFriendShipGroupMemberRespResponseVO.getCode(), addFriendShipGroupMemberRespResponseVO.getMsg());
            }
//            return ResponseVO.successResponse(new AddFriendShipGroupResp(friendShipGroup));
        }

        //TODO: 添加好友分组消息通知
        AddFriendGroupPack addFriendGroupPack = new AddFriendGroupPack();
        addFriendGroupPack.setFromId(req.getFromId());
        addFriendGroupPack.setGroupName(req.getGroupName());
        addFriendGroupPack.setSequence(sequence);
        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_ADD,
                addFriendGroupPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse(new AddFriendShipGroupResp(friendShipGroup));
    }

    @Override
    public ResponseVO<GetFriendShipGroupResp> getFriendShipGroup(GetFriendShipGroupReq req) {
        LambdaQueryWrapper<FriendShipGroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipGroupEntity::getAppId, req.getAppId())
                .eq(FriendShipGroupEntity::getFromId, req.getFromId())
                .eq(FriendShipGroupEntity::getGroupName, req.getGroupName());
        FriendShipGroupEntity friendShipGroupEntity = friendShipGroupMapper.selectOne(lambdaQueryWrapper);
        if (friendShipGroupEntity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(new GetFriendShipGroupResp(friendShipGroupEntity));
    }

    @Override
    @Transactional
    public ResponseVO<DeleteFriendShipGroupResp> deleteFriendShipGroup(DeleteFriendShipGroupReq req) {
        Map<String, List<String>> successGroups = new HashMap<>();
        Map<String, String> failureGroups = new HashMap<>();

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.FRIENDSHIPGROUP);

        for (String groupName : req.getGroupNames()) {
            List<String> toIds = new ArrayList<>();
            LambdaQueryWrapper<FriendShipGroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(FriendShipGroupEntity::getAppId, req.getAppId())
                    .eq(FriendShipGroupEntity::getGroupName, groupName)
                    .eq(FriendShipGroupEntity::getFromId, req.getFromId());
            FriendShipGroupEntity friendShipGroupEntity = friendShipGroupMapper.selectOne(lambdaQueryWrapper);
            if (friendShipGroupEntity != null) {
                friendShipGroupEntity.setUpdateTime(System.currentTimeMillis());
                friendShipGroupEntity.setSequence(sequence);
                friendShipGroupMapper.update(friendShipGroupEntity, lambdaQueryWrapper);
                int delete = friendShipGroupMapper.delete(lambdaQueryWrapper);
                if (delete != 1) {
                    failureGroups.put(groupName, FriendShipErrorCode.GROUP_DELETION_FAILED.getError());
                    continue;
                }

                writeUserSequence.writeUserSequence(req.getAppId(), req.getFromId(), Constants.SequenceConstants.FRIENDSHIPGROUP, sequence);

                ClearFriendShipGroupMemberReq clearFriendShipGroupMemberReq = new ClearFriendShipGroupMemberReq();
                clearFriendShipGroupMemberReq.setGroupId(friendShipGroupEntity.getGroupId());
                ResponseVO<ClearFriendShipGroupMemberResp> clearFriendShipGroupMemberRespResponseVO = friendShipGroupMemberService.clearFriendShipGroupMember(clearFriendShipGroupMemberReq);
                if (!clearFriendShipGroupMemberRespResponseVO.isOk()) {
                    failureGroups.put(groupName, clearFriendShipGroupMemberRespResponseVO.getMsg());
                    continue;
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

        //TODO: 删除好友分组消息通知
        DeleteFriendGroupPack deleteFriendGroupPack = new DeleteFriendGroupPack();
        deleteFriendGroupPack.setFromId(req.getFromId());
        deleteFriendGroupPack.setGroupNames(req.getGroupNames());
        deleteFriendGroupPack.setSequence(sequence);
        //TCP通知
        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_DELETE,
                deleteFriendGroupPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse(new DeleteFriendShipGroupResp(successGroups, failureGroups));
    }

    @Override
    public ResponseVO<GetAllFriendShipGroupResp> getAllFriendShipGroup(GetAllFriendShipGroupReq req) {
        Map<String, List<String>> groups = new HashMap<>();

        LambdaQueryWrapper<FriendShipGroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FriendShipGroupEntity::getAppId, req.getAppId())
                .eq(FriendShipGroupEntity::getFromId, req.getFromId());
        List<FriendShipGroupEntity> friendShipGroupEntities = friendShipGroupMapper.selectList(lambdaQueryWrapper);
        if (friendShipGroupEntities.size() == 0) {
            return ResponseVO.errorResponse(FriendShipErrorCode.YOU_HAVE_NOT_CREATED_GROUP);
        }
        GetAllFriendShipGroupMemberReq getAllFriendShipGroupMemberReq = new GetAllFriendShipGroupMemberReq();
        getAllFriendShipGroupMemberReq.setAppId(req.getAppId());
        for (FriendShipGroupEntity item : friendShipGroupEntities) {
            List<String> toIds = new ArrayList<>();
            getAllFriendShipGroupMemberReq.setGroupId(item.getGroupId());
            ResponseVO<GetAllFriendShipGroupMemberResp> allFriendShipGroupMember = friendShipGroupMemberService.getAllFriendShipGroupMember(getAllFriendShipGroupMemberReq);
            if (!allFriendShipGroupMember.isOk()) {
                groups.put(item.getGroupName(), toIds);
                continue;
            }
            List<FriendShipGroupMemberEntity> friendShipGroupMemberEntityList = allFriendShipGroupMember.getData().getFriendShipGroupMemberEntityList();
            friendShipGroupMemberEntityList.forEach(groupMember -> {
                toIds.add(groupMember.getToId());
            });
            groups.put(item.getGroupName(), toIds);
        }
        return ResponseVO.successResponse(new GetAllFriendShipGroupResp(groups));
    }
}
