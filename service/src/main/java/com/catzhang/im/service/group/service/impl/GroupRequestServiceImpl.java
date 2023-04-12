package com.catzhang.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.ApproveGroupRequestStatus;
import com.catzhang.im.common.enums.GroupErrorCode;
import com.catzhang.im.common.enums.GroupMemberRole;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.group.dao.GroupMemberEntity;
import com.catzhang.im.service.group.dao.GroupRequestEntity;
import com.catzhang.im.service.group.dao.mapper.GroupRequestMapper;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.AddGroupMemberResp;
import com.catzhang.im.service.group.model.resp.AddGroupRequestResp;
import com.catzhang.im.service.group.model.resp.ApproveGroupRequestResp;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.group.service.GroupRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class GroupRequestServiceImpl implements GroupRequestService {

    @Autowired
    GroupRequestMapper groupRequestMapper;

    @Autowired
    GroupMemberService groupMemberService;

    @Override
    public ResponseVO<AddGroupRequestResp> addGroupRequest(AddGroupRequestReq req) {

        GroupDto groupItem = req.getGroupItem();

        LambdaQueryWrapper<GroupRequestEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupRequestEntity::getAppId, req.getAppId())
                .eq(GroupRequestEntity::getFromId, req.getFromId())
                .eq(GroupRequestEntity::getToGroupId, groupItem.getToGroupId());
        GroupRequestEntity groupRequestEntity = groupRequestMapper.selectOne(lambdaQueryWrapper);
        if (groupRequestEntity == null) {
            groupRequestEntity = new GroupRequestEntity();
            groupRequestEntity.setAppId(req.getAppId());
            groupRequestEntity.setFromId(req.getFromId());
            groupRequestEntity.setToGroupId(groupItem.getToGroupId());
            groupRequestEntity.setAddSource(groupItem.getAddSource());
            groupRequestEntity.setAddWording(groupItem.getAddWording());
            groupRequestEntity.setReadStatus(0);
            groupRequestEntity.setApproveStatus(0);
            groupRequestEntity.setCreateTime(System.currentTimeMillis());
            int insert = groupRequestMapper.insert(groupRequestEntity);
            if (insert != 1) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_REQUEST_IS_FAILED);
            }
        } else {
            if (StringUtils.isNotBlank(groupItem.getAddSource())) {
                groupRequestEntity.setAddWording(groupItem.getAddWording());
            }
            if (StringUtils.isNotBlank(groupItem.getAddWording())) {
                groupRequestEntity.setAddWording(groupItem.getAddWording());
            }
            groupRequestEntity.setReadStatus(0);
            groupRequestEntity.setApproveStatus(0);
            groupRequestEntity.setUpdateTime(System.currentTimeMillis());
            int update = groupRequestMapper.update(groupRequestEntity, lambdaQueryWrapper);
            if (update != 1) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_REQUEST_IS_FAILED);
            }
        }

        return ResponseVO.successResponse(new AddGroupRequestResp(groupRequestEntity));
    }

    @Override
    public ResponseVO<ApproveGroupRequestResp> approveGroupRequest(ApproveGroupRequestReq req) {
        GroupRequestEntity groupRequestEntity = groupRequestMapper.selectById(req.getId());
        if (groupRequestEntity == null) {
            throw new ApplicationException(GroupErrorCode.ADD_GROUP_APPLICATION_IS_NOT_EXIST);
        }

        GetGroupManagerReq getGroupManagerReq = new GetGroupManagerReq();
        getGroupManagerReq.setAppId(req.getAppId());
        getGroupManagerReq.setGroupId(groupRequestEntity.getToGroupId());

        List<GroupMemberDto> groupManager = groupMemberService.getGroupManager(getGroupManagerReq);
        List<String> managerIds = new ArrayList<>();
        groupManager.forEach(item -> {
            managerIds.add(item.getMemberId());
        });

        if (!managerIds.contains(req.getOperator())) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
        }

        if (groupRequestEntity.getApproveStatus() == ApproveGroupRequestStatus.AGREE.getCode() || groupRequestEntity.getApproveStatus() == ApproveGroupRequestStatus.REJECT.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_REQUEST_APPROVED);
        }

        groupRequestEntity.setApproveStatus(req.getStatus());
        groupRequestEntity.setUpdateTime(System.currentTimeMillis());
        int update = groupRequestMapper.updateById(groupRequestEntity);
        if (update != 1) {
            return ResponseVO.errorResponse();
        }

        if (req.getStatus() == ApproveGroupRequestStatus.AGREE.getCode()) {
            AddGroupMemberReq addGroupMemberReq = new AddGroupMemberReq();
            addGroupMemberReq.setAppId(req.getAppId());
            addGroupMemberReq.setGroupId(groupRequestEntity.getToGroupId());
            GroupMemberDto groupMemberDto = new GroupMemberDto();
            groupMemberDto.setRole(GroupMemberRole.ORDINARY.getCode());
            groupMemberDto.setMemberId(groupRequestEntity.getFromId());
            addGroupMemberReq.setGroupMember(groupMemberDto);
            ResponseVO<AddGroupMemberResp> addGroupMemberRespResponseVO = groupMemberService.addGroupMember(addGroupMemberReq);
            if (!addGroupMemberRespResponseVO.isOk()) {
                return ResponseVO.errorResponse(addGroupMemberRespResponseVO.getCode(), addGroupMemberRespResponseVO.getMsg());
            }

            GroupMemberEntity groupMemberEntity = addGroupMemberRespResponseVO.getData().getGroupMemberEntity();
            return ResponseVO.successResponse(new ApproveGroupRequestResp(groupRequestEntity, groupMemberEntity));
        }

        return ResponseVO.successResponse(new ApproveGroupRequestResp(groupRequestEntity, null));
    }
}
