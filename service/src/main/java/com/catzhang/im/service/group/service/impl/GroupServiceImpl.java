package com.catzhang.im.service.group.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.*;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.group.dao.GroupEntity;
import com.catzhang.im.service.group.dao.GroupMemberEntity;
import com.catzhang.im.service.group.dao.mapper.GroupMapper;
import com.catzhang.im.service.group.dao.mapper.GroupMemberMapper;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.group.service.GroupRequestService;
import com.catzhang.im.service.group.service.GroupService;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import com.catzhang.im.service.utils.CallbackService;
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

        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setCreateTime(System.currentTimeMillis());
        groupEntity.setStatus(GroupStatus.NORMAL.getCode());
        BeanUtils.copyProperties(req, groupEntity);
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
        groupEntity.setUpdateTime(System.currentTimeMillis());
        int update = groupMapper.update(groupEntity, lambdaQueryWrapper);
        if (update != 1) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
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

        groupEntity.setStatus(GroupStatus.DESTROY.getCode());
        int update = groupMapper.update(groupEntity, lambdaQueryWrapper);
        if (update != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
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

        groupEntity.setOwnerId(req.getOwnerId());
        groupEntity.setUpdateTime(System.currentTimeMillis());
        groupMapper.update(groupEntity, lambdaQueryWrapper);
        TransferGroupMemberReq transferGroupMemberReq = new TransferGroupMemberReq();
        transferGroupMemberReq.setOwner(req.getOwnerId());
        transferGroupMemberReq.setAppId(req.getAppId());
        transferGroupMemberReq.setGroupId(req.getGroupId());
        ResponseVO<TransferGroupMemberResp> transferGroupMemberRespResponseVO = groupMemberService.transferGroupMember(transferGroupMemberReq);

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
        LambdaUpdateWrapper<GroupEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(GroupEntity::getAppId, req.getAppId())
                .eq(GroupEntity::getGroupId, req.getGroupId())
                .set(GroupEntity::getMute, req.getMute());
        groupMapper.update(null, lambdaUpdateWrapper);

        LambdaUpdateWrapper<GroupMemberEntity> groupMemberEntityLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        groupMemberEntityLambdaUpdateWrapper.eq(GroupMemberEntity::getAppId, req.getAppId())
                .eq(GroupMemberEntity::getGroupId, req.getGroupId())
                .ne(GroupMemberEntity::getRole, GroupMemberRole.LEAVE.getCode())
                .set(GroupMemberEntity::getMute, req.getMute());
        groupMemberMapper.update(null, groupMemberEntityLambdaUpdateWrapper);

        return ResponseVO.successResponse(new MuteGroupResp(data));
    }

    @Override
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
}
