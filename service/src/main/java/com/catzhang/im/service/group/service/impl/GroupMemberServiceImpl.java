package com.catzhang.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.GroupErrorCode;
import com.catzhang.im.common.enums.GroupMemberRole;
import com.catzhang.im.common.enums.GroupType;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.group.dao.GroupMemberEntity;
import com.catzhang.im.service.group.dao.mapper.GroupMemberMapper;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.group.service.GroupService;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    @Autowired
    GroupMemberMapper groupMemberMapper;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Override
    @Transactional
    public ResponseVO<AddGroupMemberResp> addGroupMember(AddGroupMemberReq req) {

        GroupMemberDto groupMember = req.getGroupMember();

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setUserId(groupMember.getMemberId());
        getSingleUserInfoReq.setAppId(req.getAppId());
        ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!singleUserInfo.isOk()) {
            return ResponseVO.errorResponse(singleUserInfo.getCode(), singleUserInfo.getMsg());
        }

        LambdaQueryWrapper<GroupMemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(GroupMemberEntity::getAppId, req.getAppId())
                .like(GroupMemberEntity::getGroupId, req.getGroupId());

        if (groupMember.getRole() != null && groupMember.getRole() == GroupMemberRole.OWNER.getCode()) {
            lambdaQueryWrapper.like(GroupMemberEntity::getRole, groupMember.getRole());
            Integer integer = groupMemberMapper.selectCount(lambdaQueryWrapper);
            if (integer > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_HAVE_OWNER);
            }
        }

        lambdaQueryWrapper.like(GroupMemberEntity::getMemberId, groupMember.getMemberId());
        GroupMemberEntity groupMemberEntity = groupMemberMapper.selectOne(lambdaQueryWrapper);

        if (groupMemberEntity == null) {
            groupMemberEntity = new GroupMemberEntity();
            BeanUtils.copyProperties(groupMember, groupMemberEntity);
            groupMemberEntity.setGroupId(req.getGroupId());
            groupMemberEntity.setAppId(req.getAppId());
            groupMemberEntity.setJoinTime(System.currentTimeMillis());
            int insert = groupMemberMapper.insert(groupMemberEntity);
            if (insert == 1) {
                return ResponseVO.successResponse(new AddGroupMemberResp(groupMemberEntity));
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        } else if (GroupMemberRole.LEAVE.getCode() == groupMember.getRole()) {
            BeanUtils.copyProperties(groupMember, groupMemberEntity);
            groupMemberEntity.setJoinTime(System.currentTimeMillis());
            int update = groupMemberMapper.update(groupMemberEntity, lambdaQueryWrapper);
            if (update == 1) {
                return ResponseVO.successResponse(new AddGroupMemberResp(groupMemberEntity));
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        }

        return ResponseVO.errorResponse(GroupErrorCode.USER_IS_JOINED_GROUP);
    }

    @Override
    public ResponseVO<GetRoleInGroupResp> getRoleInGroup(GetRoleInGroupReq req) {
        GetRoleInGroupResp getRoleInGroupResp = new GetRoleInGroupResp();

        LambdaQueryWrapper<GroupMemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(GroupMemberEntity::getAppId, req.getAppId())
                .like(GroupMemberEntity::getGroupId, req.getGroupId())
                .like(GroupMemberEntity::getMemberId, req.getMemberId());
        GroupMemberEntity groupMemberEntity = groupMemberMapper.selectOne(lambdaQueryWrapper);
        if (groupMemberEntity == null || groupMemberEntity.getRole() == GroupMemberRole.LEAVE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }

        getRoleInGroupResp.setGroupMemberId(groupMemberEntity.getGroupMemberId());
        getRoleInGroupResp.setRole(groupMemberEntity.getRole());
        getRoleInGroupResp.setSpeakDate(groupMemberEntity.getSpeakDate());
        getRoleInGroupResp.setMemberId(groupMemberEntity.getMemberId());

        return ResponseVO.successResponse(getRoleInGroupResp);
    }

    @Override
    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req) {

        if (req.getLimit() != null) {
            Page<GroupMemberEntity> resultPage = new Page<>(req.getOffset(), req.getLimit());
            LambdaQueryWrapper<GroupMemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.like(GroupMemberEntity::getAppId, req.getAppId())
                    .like(GroupMemberEntity::getMemberId, req.getMemberId());
            groupMemberMapper.selectPage(resultPage, lambdaQueryWrapper);
            HashSet<String> groupIds = new HashSet<>();
            List<GroupMemberEntity> records = resultPage.getRecords();
            for (GroupMemberEntity record : records) {
                groupIds.add(record.getGroupId());
            }
            return ResponseVO.successResponse(groupIds);
        } else {
            return ResponseVO.successResponse(groupMemberMapper.getJoinedGroupId(req.getAppId(), req.getMemberId()));
        }
    }

    @Override
    public ResponseVO<TransferGroupMemberResp> transferGroupMember(TransferGroupMemberReq req) {

        LambdaUpdateWrapper<GroupMemberEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.like(GroupMemberEntity::getAppId, req.getAppId())
                .like(GroupMemberEntity::getGroupId, req.getGroupId())
                .like(GroupMemberEntity::getRole, GroupMemberRole.OWNER.getCode())
                .set(GroupMemberEntity::getRole, GroupMemberRole.ORDINARY.getCode());
        groupMemberMapper.update(null, lambdaUpdateWrapper);

        LambdaUpdateWrapper<GroupMemberEntity> newOwnerUpdateWrapper = new LambdaUpdateWrapper<>();
        newOwnerUpdateWrapper.like(GroupMemberEntity::getAppId, req.getAppId())
                .like(GroupMemberEntity::getGroupId, req.getGroupId())
                .like(GroupMemberEntity::getMemberId, req.getOwner())
                .set(GroupMemberEntity::getRole, GroupMemberRole.OWNER.getCode());
        groupMemberMapper.update(null, newOwnerUpdateWrapper);

        LambdaQueryWrapper<GroupMemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(GroupMemberEntity::getAppId, req.getAppId())
                .like(GroupMemberEntity::getGroupId, req.getGroupId())
                .like(GroupMemberEntity::getMemberId, req.getOwner());
        GroupMemberEntity groupMemberEntity = groupMemberMapper.selectOne(lambdaQueryWrapper);

        return ResponseVO.successResponse(new TransferGroupMemberResp(groupMemberEntity));
    }

    @Override
    public ResponseVO<List<GroupMemberDto>> getGroupMember(GetGroupMemberReq req) {
        List<GroupMemberDto> groupMember = groupMemberMapper.getGroupMember(req.getAppId(), req.getGroupId());
        return ResponseVO.successResponse(groupMember);
    }

    @Override
    public ResponseVO<List<ImportGroupMemberResp>> importGroupMember(ImportGroupMemberReq req) {

        List<ImportGroupMemberResp> resp = new ArrayList<>();
        GetGroupReq getGroupReq = new GetGroupReq();
        getGroupReq.setAppId(req.getAppId());
        getGroupReq.setGroupId(req.getGroupId());
        AddGroupMemberReq addGroupMemberReq = new AddGroupMemberReq();
        BeanUtils.copyProperties(getGroupReq, addGroupMemberReq);

        ResponseVO<GetGroupResp> group = groupService.getGroup(getGroupReq);
        if (!group.isOk()) {
            return ResponseVO.errorResponse(group.getCode(), group.getMsg());
        }
        for (GroupMemberDto member : req.getMembers()) {
            addGroupMemberReq.setGroupMember(member);
            ResponseVO<AddGroupMemberResp> addGroupMemberRespResponseVO = this.addGroupMember(addGroupMemberReq);
            ImportGroupMemberResp importGroupMemberResp = new ImportGroupMemberResp();
            importGroupMemberResp.setMemberId(member.getMemberId());
            if (!addGroupMemberRespResponseVO.isOk()) {
                importGroupMemberResp.setResult(0);
            } else if (addGroupMemberRespResponseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                importGroupMemberResp.setResult(2);
            } else {
                importGroupMemberResp.setResult(1);
            }
            importGroupMemberResp.setResultMessage(addGroupMemberRespResponseVO.getMsg());
            resp.add(importGroupMemberResp);
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<List<AddMemberResp>> addMember(AddMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();
        GetGroupReq getGroupReq = new GetGroupReq();
        getGroupReq.setAppId(req.getAppId());
        getGroupReq.setGroupId(req.getGroupId());

        AddGroupMemberReq addGroupMemberReq = new AddGroupMemberReq();
        BeanUtils.copyProperties(getGroupReq, addGroupMemberReq);

        ResponseVO<GetGroupResp> group = groupService.getGroup(getGroupReq);
        if (!group.isOk()) {
            return ResponseVO.errorResponse(group.getCode(), group.getMsg());
        }

        List<GroupMemberDto> members = req.getMembers();
        GetGroupResp data = group.getData();
        boolean isAdmin = false;

        if (!isAdmin && data.getGroupType() == GroupType.PUBLIC.getCode()) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
        }


        members.forEach(member -> {
            addGroupMemberReq.setGroupMember(member);
            ResponseVO<AddGroupMemberResp> addGroupMemberRespResponseVO = this.addGroupMember(addGroupMemberReq);
            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(member.getMemberId());
            if (!addGroupMemberRespResponseVO.isOk()) {
                addMemberResp.setResult(0);
            } else if (addGroupMemberRespResponseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                addMemberResp.setResult(2);
            } else {
                addMemberResp.setResult(1);
            }
            addMemberResp.setResultMessage(addGroupMemberRespResponseVO.getMsg());
            resp.add(addMemberResp);
        });

        return ResponseVO.successResponse(resp);
    }
}
