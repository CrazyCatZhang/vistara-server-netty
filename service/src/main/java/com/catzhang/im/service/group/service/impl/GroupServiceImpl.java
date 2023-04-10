package com.catzhang.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.GroupErrorCode;
import com.catzhang.im.common.enums.GroupMemberRole;
import com.catzhang.im.common.enums.GroupStatus;
import com.catzhang.im.common.enums.GroupType;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.group.dao.GroupEntity;
import com.catzhang.im.service.group.dao.GroupMemberEntity;
import com.catzhang.im.service.group.dao.mapper.GroupMapper;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.group.service.GroupService;
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

    @Override
    public ResponseVO<ImportGroupResp> importGroup(ImportGroupReq req) {

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            lambdaQueryWrapper.like(GroupEntity::getGroupId, req.getGroupId())
                    .like(GroupEntity::getAppId, req.getAppId());
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
    public ResponseVO<CreateGroupResp> createGroup(CreateGroupReq req) {

        boolean isAdmin = false;

        if (!isAdmin) {
            req.setOwnerId(req.getOperator());
        }

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            lambdaQueryWrapper.like(GroupEntity::getGroupId, req.getGroupId())
                    .like(GroupEntity::getAppId, req.getAppId());
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

        return ResponseVO.successResponse(new CreateGroupResp(groupInfo, failureMembers));
    }

    @Override
    @Transactional
    public ResponseVO<UpdateGroupInfoResp> updateGroupInfo(UpdateGroupInfoReq req) {

        LambdaQueryWrapper<GroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(GroupEntity::getAppId, req.getAppId())
                .like(GroupEntity::getGroupId, req.getGroupId());
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
}
