package com.catzhang.im.service.group.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.catzhang.im.codec.pack.group.*;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.*;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
import com.catzhang.im.service.group.dao.GroupEntity;
import com.catzhang.im.service.group.dao.GroupMemberEntity;
import com.catzhang.im.service.group.dao.mapper.GroupMapper;
import com.catzhang.im.service.group.dao.mapper.GroupMemberMapper;
import com.catzhang.im.service.group.model.callback.DestroyGroupCallbackDto;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.group.service.GroupRequestService;
import com.catzhang.im.service.group.service.GroupService;
import com.catzhang.im.service.sequence.RedisSequence;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import com.catzhang.im.service.utils.CallbackService;
import com.catzhang.im.service.utils.GroupMessageProducer;
import com.catzhang.im.service.utils.WriteUserSequence;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author crazycatzhang
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    GroupMemberMapper groupMemberMapper;

    @Autowired
    UserService userService;

    @Autowired
    GroupRequestService groupRequestService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Autowired
    RedisSequence redisSequence;

    @Autowired
    WriteUserSequence writeUserSequence;

    @Override
    public ResponseVO importGroup(ImportGroupReq req) {

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setAppId(req.getAppId());
        getSingleUserInfoReq.setUserId(req.getOwnerId());
        ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!singleUserInfo.isOk()) {
            return ResponseVO.errorResponse(singleUserInfo.getCode(), singleUserInfo.getMsg());
        }

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            lambdaQueryWrapper.eq(GroupEntity::getGroupId, req.getGroupId())
                    .eq(GroupEntity::getAppId, req.getAppId());
            Integer integer = groupMapper.selectCount(lambdaQueryWrapper);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        GroupEntity groupEntity = new GroupEntity();
        if (req.getGroupType() == GroupType.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        if (req.getCreateTime() == null) {
            groupEntity.setCreateTime(System.currentTimeMillis());
        }

        groupEntity.setStatus(GroupStatus.NORMAL.getCode());
        BeanUtils.copyProperties(req, groupEntity);
        int insert = groupMapper.insert(groupEntity);

        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        return ResponseVO.successResponse(new ImportGroupResp(groupEntity));
    }

    @Override
    @Transactional
    public ResponseVO<CreateGroupResp> createGroup(CreateGroupReq req) {

        boolean isAdmin = false;

        if (!isAdmin) {
            req.setOwnerId(req.getOperator());
        }

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setAppId(req.getAppId());
        getSingleUserInfoReq.setUserId(req.getOwnerId());
        ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!singleUserInfo.isOk()) {
            return ResponseVO.errorResponse(singleUserInfo.getCode(), singleUserInfo.getMsg());
        }

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            lambdaQueryWrapper.eq(GroupEntity::getGroupId, req.getGroupId())
                    .eq(GroupEntity::getAppId, req.getAppId());
            Integer integer = groupMapper.selectCount(lambdaQueryWrapper);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        if (req.getGroupType() == GroupType.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.GROUP);

        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setCreateTime(System.currentTimeMillis());
        groupEntity.setStatus(GroupStatus.NORMAL.getCode());
        BeanUtils.copyProperties(req, groupEntity);
        groupEntity.setSequence(sequence);
        int insert = groupMapper.insert(groupEntity);
        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.CREATE_GROUP_IS_FAILED);
        }

        GroupMemberDto groupMemberDto = new GroupMemberDto();
        groupMemberDto.setMemberId(req.getOwnerId());
        groupMemberDto.setRole(GroupMemberRole.OWNER.getCode());
        groupMemberDto.setJoinTime(System.currentTimeMillis());
        AddGroupMemberReq addGroupMemberReq = new AddGroupMemberReq();
        addGroupMemberReq.setGroupMember(groupMemberDto);
        addGroupMemberReq.setGroupId(req.getGroupId());
        addGroupMemberReq.setAppId(req.getAppId());
        ResponseVO<AddGroupMemberResp> addGroupMemberRespResponseVO = groupMemberService.addGroupMember(addGroupMemberReq);
        if (!addGroupMemberRespResponseVO.isOk()) {
            return ResponseVO.errorResponse(addGroupMemberRespResponseVO.getCode(), addGroupMemberRespResponseVO.getMsg());
        }

        Map<String, List<GroupMemberEntity>> groupInfo = new HashMap<>();
        List<GroupMemberEntity> members = new ArrayList<>();
        List<String> failureMembers = new ArrayList<>();

        members.add(addGroupMemberRespResponseVO.getData().getGroupMemberEntity());

        for (GroupMemberDto member : req.getMembers()) {
            addGroupMemberReq.setGroupMember(member);
            ResponseVO<AddGroupMemberResp> addGroupMemberResponseVO = groupMemberService.addGroupMember(addGroupMemberReq);
            if (!addGroupMemberResponseVO.isOk()) {
                failureMembers.add(addGroupMemberResponseVO.getData().getGroupMemberEntity().getMemberId());
                continue;
            }
            members.add(addGroupMemberResponseVO.getData().getGroupMemberEntity());
        }

        groupInfo.put(req.getGroupName(), members);

        //TODO: 创建群通知
        CreateGroupPack createGroupPack = new CreateGroupPack();
        BeanUtils.copyProperties(groupEntity, createGroupPack);
        createGroupPack.setSequence(sequence);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.CREATED_GROUP, createGroupPack
                , new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        //TODO: 创建群之后回调
        if (appConfig.isCreateGroupAfterCallback()) {
            callbackService.afterCallback(req.getAppId(), Constants.CallbackCommand.CREATEGROUPAFTER,
                    JSONObject.toJSONString(groupEntity));
        }

        return ResponseVO.successResponse(new CreateGroupResp(groupInfo, failureMembers));
    }

    @Override
    @Transactional
    public ResponseVO<UpdateGroupInfoResp> updateGroupInfo(UpdateGroupInfoReq req) {

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupEntity::getAppId, req.getAppId())
                .eq(GroupEntity::getGroupId, req.getGroupId());
        GroupEntity groupEntity = groupMapper.selectOne(lambdaQueryWrapper);
        if (groupEntity == null) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        if (groupEntity.getStatus() == GroupStatus.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        boolean isAdmin = false;
        if (!isAdmin) {
            GetRoleInGroupReq getRoleInGroupReq = new GetRoleInGroupReq();
            getRoleInGroupReq.setGroupId(req.getGroupId());
            getRoleInGroupReq.setAppId(req.getAppId());
            getRoleInGroupReq.setMemberId(req.getOperator());
            ResponseVO<GetRoleInGroupResp> roleInGroup = groupMemberService.getRoleInGroup(getRoleInGroupReq);
            if (!roleInGroup.isOk()) {
                return ResponseVO.errorResponse(roleInGroup.getCode(), roleInGroup.getMsg());
            }

            GetRoleInGroupResp data = roleInGroup.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRole.ADMINISTRATOR.getCode() || roleInfo == GroupMemberRole.OWNER.getCode();
            if (!isManager && groupEntity.getGroupType() == GroupType.PUBLIC.getCode()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        BeanUtils.copyProperties(req, groupEntity);
        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.GROUP);
        groupEntity.setUpdateTime(System.currentTimeMillis());
        groupEntity.setSequence(sequence);

        int update = groupMapper.update(groupEntity, lambdaQueryWrapper);
        if (update != 1) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
        }

        //TODO: 更新群通知
        UpdateGroupInfoPack pack = new UpdateGroupInfoPack();
        BeanUtils.copyProperties(req, pack);
        pack.setSequence(sequence);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.UPDATED_GROUP,
                pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        //TODO: 更新群之后回调
        if (appConfig.isModifyGroupAfterCallback()) {
            callbackService.afterCallback(req.getAppId(), Constants.CallbackCommand.UPDATEGROUPAFTER,
                    JSONObject.toJSONString(groupMapper.selectOne(lambdaQueryWrapper)));
        }

        return ResponseVO.successResponse(new UpdateGroupInfoResp(groupEntity));
    }

    @Override
    public ResponseVO<GetJoinedGroupResp> getJoinedGroup(GetJoinedGroupReq req) {

        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.getMemberJoinedGroup(req);
        if (!memberJoinedGroup.isOk()) {
            return ResponseVO.errorResponse(memberJoinedGroup.getCode(), memberJoinedGroup.getMsg());
        }

        GetJoinedGroupResp resp = new GetJoinedGroupResp();

        if (CollectionUtils.isEmpty(memberJoinedGroup.getData())) {
            resp.setTotalCount(0);
            resp.setGroupList(new ArrayList<>());
            return ResponseVO.successResponse(resp);
        }

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupEntity::getAppId, req.getAppId())
                .in(GroupEntity::getGroupId, memberJoinedGroup.getData());

        if (CollectionUtils.isNotEmpty(req.getGroupType())) {
            lambdaQueryWrapper.in(GroupEntity::getGroupType, req.getGroupType());
        }

        List<GroupEntity> groupEntities = groupMapper.selectList(lambdaQueryWrapper);
        resp.setGroupList(groupEntities);
        resp.setTotalCount(groupEntities.size());

        return ResponseVO.successResponse(resp);
    }

    @Override
    @Transactional
    public ResponseVO<DestroyGroupResp> destroyGroup(DestroyGroupReq req) {

        boolean isAdmin = false;

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupEntity::getAppId, req.getAppId())
                .eq(GroupEntity::getGroupId, req.getGroupId());
        GroupEntity groupEntity = groupMapper.selectOne(lambdaQueryWrapper);
        if (groupEntity == null) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        if (groupEntity.getStatus() == GroupStatus.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        if (!isAdmin) {
            if (groupEntity.getGroupType() == GroupType.PRIVATE.getCode()) {
                throw new ApplicationException(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_DESTORY);
            }
            if (groupEntity.getGroupType() == GroupType.PUBLIC.getCode() && !groupEntity.getOwnerId().equals(req.getOperator())) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.GROUP);

        groupEntity.setStatus(GroupStatus.DESTROY.getCode());
        groupEntity.setSequence(sequence);
        int update = groupMapper.update(groupEntity, lambdaQueryWrapper);
        if (update != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        //TODO: 解散群通知
        DestroyGroupPack pack = new DestroyGroupPack();
        pack.setGroupId(req.getGroupId());
        pack.setSequence(sequence);
        groupMessageProducer.producer(req.getOperator(),
                GroupEventCommand.DESTROY_GROUP, pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        //TODO: 解散群之后应该清空该群的所有成员
        GetGroupMemberIdReq getGroupMemberIdReq = new GetGroupMemberIdReq();
        getGroupMemberIdReq.setAppId(req.getAppId());
        getGroupMemberIdReq.setGroupId(req.getGroupId());
        RemoveMemberReq removeMemberReq = new RemoveMemberReq();
        BeanUtils.copyProperties(getGroupMemberIdReq, removeMemberReq);
        removeMemberReq.setOperator(req.getOperator());
        List<String> groupMemberIds = groupMemberService.getGroupMemberId(getGroupMemberIdReq);
        for (String groupMemberId : groupMemberIds) {
            removeMemberReq.setMemberId(groupMemberId);
            ResponseVO<RemoveMemberResp> removeMemberRespResponseVO = groupMemberService.removeGroupMember(removeMemberReq);
            if (!removeMemberRespResponseVO.isOk()) {
                return ResponseVO.errorResponse(removeMemberRespResponseVO.getCode(), removeMemberRespResponseVO.getMsg());
            }
        }

        //TODO: 解散群之后回调
        if (appConfig.isDestroyGroupAfterCallback()) {
            DestroyGroupCallbackDto dto = new DestroyGroupCallbackDto();
            dto.setGroupId(req.getGroupId());
            callbackService.afterCallback(req.getAppId()
                    , Constants.CallbackCommand.DESTROYGROUPAFTER,
                    JSONObject.toJSONString(dto));
        }

        return ResponseVO.successResponse(new UpdateGroupInfoResp(groupEntity));
    }

    @Override
    @Transactional
    public ResponseVO<TransferGroupResp> transferGroup(TransferGroupReq req) {


        boolean isMyself = req.getOwnerId().equals(req.getOperator());

        GetRoleInGroupReq getRoleInGroupReq = new GetRoleInGroupReq();
        getRoleInGroupReq.setMemberId(req.getOperator());
        getRoleInGroupReq.setAppId(req.getAppId());
        getRoleInGroupReq.setGroupId(req.getGroupId());
        ResponseVO<GetRoleInGroupResp> roleInGroup = groupMemberService.getRoleInGroup(getRoleInGroupReq);
        if (!roleInGroup.isOk()) {
            return ResponseVO.errorResponse(roleInGroup.getCode(), roleInGroup.getMsg());
        }

        if (roleInGroup.getData().getRole() != GroupMemberRole.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }

        if (isMyself) {
            return ResponseVO.errorResponse(GroupErrorCode.NOT_TRANSFERABLE_TO_ONESELF);
        }

        getRoleInGroupReq.setMemberId(req.getOwnerId());
        ResponseVO<GetRoleInGroupResp> newOwner = groupMemberService.getRoleInGroup(getRoleInGroupReq);
        if (!newOwner.isOk()) {
            return ResponseVO.errorResponse(newOwner.getCode(), newOwner.getMsg());
        }

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupEntity::getAppId, req.getAppId())
                .eq(GroupEntity::getGroupId, req.getGroupId());
        GroupEntity groupEntity = groupMapper.selectOne(lambdaQueryWrapper);
        if (groupEntity.getStatus() == GroupStatus.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.GROUP);

        groupEntity.setOwnerId(req.getOwnerId());
        groupEntity.setSequence(sequence);
        groupEntity.setUpdateTime(System.currentTimeMillis());
        groupMapper.update(groupEntity, lambdaQueryWrapper);
        TransferGroupMemberReq transferGroupMemberReq = new TransferGroupMemberReq();
        transferGroupMemberReq.setOwner(req.getOwnerId());
        transferGroupMemberReq.setAppId(req.getAppId());
        transferGroupMemberReq.setGroupId(req.getGroupId());
        ResponseVO<TransferGroupMemberResp> transferGroupMemberRespResponseVO = groupMemberService.transferGroupMember(transferGroupMemberReq);

        //TODO: 转让群TCP通知
        TransferGroupPack transferGroupPack = new TransferGroupPack();
        transferGroupPack.setGroupId(req.getGroupId());
        transferGroupPack.setOwnerId(req.getOwnerId());
        transferGroupPack.setSequence(sequence);
        groupMessageProducer.producer(req.getOperator(),
                GroupEventCommand.TRANSFER_GROUP, transferGroupPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse(new TransferGroupResp(groupEntity, transferGroupMemberRespResponseVO.getData().getGroupMember()));
    }

    @Override
    public ResponseVO<GetGroupResp> getGroup(GetGroupReq req) {

        ResponseVO<GroupEntity> groupEntityResponseVO = this.handleGetGroup(req);
        if (!groupEntityResponseVO.isOk()) {
            return ResponseVO.errorResponse(groupEntityResponseVO.getCode(), groupEntityResponseVO.getMsg());
        }
        GetGroupResp getGroupResp = new GetGroupResp();
        BeanUtils.copyProperties(groupEntityResponseVO.getData(), getGroupResp);
        try {
            GetGroupMemberReq getGroupMemberReq = new GetGroupMemberReq();
            getGroupMemberReq.setAppId(req.getAppId());
            getGroupMemberReq.setGroupId(req.getGroupId());
            ResponseVO<List<GroupMemberDto>> groupMember = groupMemberService.getGroupMember(getGroupMemberReq);
            if (groupMember.isOk()) {
                getGroupResp.setMemberList(groupMember.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseVO.successResponse(getGroupResp);
    }

    @Override
    public ResponseVO<GroupEntity> handleGetGroup(GetGroupReq req) {

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupEntity::getAppId, req.getAppId())
                .eq(GroupEntity::getGroupId, req.getGroupId());
        GroupEntity groupEntity = groupMapper.selectOne(lambdaQueryWrapper);
        if (groupEntity == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(groupEntity);
    }

    @Override
    public ResponseVO<MuteGroupResp> muteGroup(MuteGroupReq req) {

        GetGroupReq getGroupReq = new GetGroupReq();
        getGroupReq.setGroupId(req.getGroupId());
        getGroupReq.setAppId(req.getAppId());
        ResponseVO<GroupEntity> groupEntityResponseVO = this.handleGetGroup(getGroupReq);
        if (!groupEntityResponseVO.isOk()) {
            return ResponseVO.errorResponse(groupEntityResponseVO.getCode(), groupEntityResponseVO.getMsg());
        }

        GroupEntity data = groupEntityResponseVO.getData();
        if (data.getStatus() == GroupStatus.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        if (data.getGroupType() == GroupType.PRIVATE.getCode()) {
            throw new ApplicationException(GroupErrorCode.PRIVATE_GROUPS_ARE_NOT_ALLOWED_TO_MUTE);
        }

        boolean isAdmin = false;

        if (!isAdmin) {
            GetRoleInGroupReq getRoleInGroupReq = new GetRoleInGroupReq();
            getRoleInGroupReq.setMemberId(req.getOperator());
            getRoleInGroupReq.setGroupId(req.getGroupId());
            getRoleInGroupReq.setAppId(req.getAppId());
            ResponseVO<GetRoleInGroupResp> roleInGroup = groupMemberService.getRoleInGroup(getRoleInGroupReq);
            if (!roleInGroup.isOk()) {
                return ResponseVO.errorResponse(roleInGroup.getCode(), roleInGroup.getMsg());
            }

            Integer role = roleInGroup.getData().getRole();
            boolean isManage = role == GroupMemberRole.ADMINISTRATOR.getCode() || role == GroupMemberRole.OWNER.getCode();

            if (!isManage && data.getGroupType() == GroupType.PUBLIC.getCode()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        data.setMute(req.getMute());

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.GROUP);

        LambdaUpdateWrapper<GroupEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(GroupEntity::getAppId, req.getAppId())
                .eq(GroupEntity::getGroupId, req.getGroupId())
                .set(GroupEntity::getMute, req.getMute())
                .set(GroupEntity::getSequence, sequence);
        groupMapper.update(null, lambdaUpdateWrapper);

        LambdaUpdateWrapper<GroupMemberEntity> groupMemberEntityLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        groupMemberEntityLambdaUpdateWrapper.eq(GroupMemberEntity::getAppId, req.getAppId())
                .eq(GroupMemberEntity::getGroupId, req.getGroupId())
                .ne(GroupMemberEntity::getRole, GroupMemberRole.LEAVE.getCode())
                .set(GroupMemberEntity::getMute, req.getMute());
        groupMemberMapper.update(null, groupMemberEntityLambdaUpdateWrapper);

        //TODO: 禁言群TCP通知
        MuteGroupPack muteGroupPack = new MuteGroupPack();
        muteGroupPack.setGroupId(req.getGroupId());
        muteGroupPack.setSequence(sequence);
        groupMessageProducer.producer(req.getOperator(),
                GroupEventCommand.MUTE_GROUP, muteGroupPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse(new MuteGroupResp(data));
    }

    @Override
    @Transactional
    public ResponseVO<AddGroupResp> addGroup(AddGroupReq req) {

        GetGroupReq getGroupReq = new GetGroupReq();
        getGroupReq.setAppId(req.getAppId());
        getGroupReq.setGroupId(req.getGroupItem().getToGroupId());
        ResponseVO<GetGroupResp> group = this.getGroup(getGroupReq);
        if (!group.isOk()) {
            return ResponseVO.errorResponse(group.getCode(), group.getMsg());
        }

        GetGroupResp groupData = group.getData();

        if (groupData.getGroupType() == GroupType.PRIVATE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.PRIVATE_GROUPS_ARE_INVITE_ONLY);
        }

        if (groupData.getApplyJoinType() != null) {
            if (groupData.getApplyJoinType() == AllowGroupType.NOT_NEED_APPROVAL.getCode()) {
                AddGroupMemberReq addGroupMemberReq = new AddGroupMemberReq();
                addGroupMemberReq.setAppId(req.getAppId());
                addGroupMemberReq.setGroupId(groupData.getGroupId());
                GroupMemberDto groupMemberDto = new GroupMemberDto();
                groupMemberDto.setRole(GroupMemberRole.ORDINARY.getCode());
                groupMemberDto.setMemberId(req.getFromId());
                addGroupMemberReq.setGroupMember(groupMemberDto);
                ResponseVO<AddGroupMemberResp> addGroupMemberRespResponseVO = groupMemberService.addGroupMember(addGroupMemberReq);
                if (!addGroupMemberRespResponseVO.isOk()) {
                    return ResponseVO.errorResponse(addGroupMemberRespResponseVO.getCode(), addGroupMemberRespResponseVO.getMsg());
                }
                GroupMemberEntity groupMemberEntity = addGroupMemberRespResponseVO.getData().getGroupMemberEntity();
                return ResponseVO.successResponse(new AddGroupMemberResp(groupMemberEntity));
            } else if (groupData.getApplyJoinType() == AllowGroupType.NEED_APPROVAL.getCode()) {
                LambdaQueryWrapper<GroupMemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(GroupMemberEntity::getAppId, req.getAppId())
                        .eq(GroupMemberEntity::getGroupId, groupData.getGroupId())
                        .eq(GroupMemberEntity::getMemberId, req.getFromId());

                GroupMemberEntity groupMemberEntity = groupMemberMapper.selectOne(lambdaQueryWrapper);
                if (groupMemberEntity == null || groupMemberEntity.getRole() == GroupMemberRole.LEAVE.getCode()) {
                    AddGroupRequestReq addGroupRequestReq = new AddGroupRequestReq();
                    addGroupRequestReq.setGroupItem(req.getGroupItem());
                    addGroupRequestReq.setFromId(req.getFromId());
                    addGroupRequestReq.setAppId(req.getAppId());
                    ResponseVO<AddGroupRequestResp> addGroupRequestRespResponseVO = groupRequestService.addGroupRequest(addGroupRequestReq);
                    if (!addGroupRequestRespResponseVO.isOk()) {
                        return ResponseVO.errorResponse(addGroupRequestRespResponseVO.getCode(), addGroupRequestRespResponseVO.getMsg());
                    }
                    return ResponseVO.successResponse(addGroupRequestRespResponseVO.getData());
                } else {
                    return ResponseVO.errorResponse(GroupErrorCode.USER_IS_JOINED_GROUP);
                }
            } else {
                return ResponseVO.errorResponse(GroupErrorCode.NO_ONE_IS_ALLOWED_IN_THIS_GROUP);
            }
        }
        return null;
    }

    @Override
    public ResponseVO<SyncResp<GroupEntity>> syncJoinedGroupList(SyncReq req) {
        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }

        SyncResp<GroupEntity> resp = new SyncResp<>();

        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.syncMemberJoinedGroup(req.getOperator(), req.getAppId());
        if (memberJoinedGroup.isOk()) {

            Collection<String> data = memberJoinedGroup.getData();
            QueryWrapper<GroupEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", req.getAppId());
            queryWrapper.in("group_id", data);
            queryWrapper.gt("sequence", req.getLastSequence());
            queryWrapper.last(" limit " + req.getMaxLimit());
            queryWrapper.orderByAsc("sequence");

            List<GroupEntity> list = groupMapper.selectList(queryWrapper);

            if (!CollectionUtils.isEmpty(list)) {
                GroupEntity maxSequenceEntity = list.get(list.size() - 1);
                resp.setDataList(list);
                //设置最大seq
                Long maxSequence = groupMapper.getGroupMaxSequence(data, req.getAppId());
                resp.setMaxSequence(maxSequence);
                //设置是否拉取完毕
                resp.setCompleted(maxSequenceEntity.getSequence() >= maxSequence);
                return ResponseVO.successResponse(resp);
            }

        }
        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }
}
