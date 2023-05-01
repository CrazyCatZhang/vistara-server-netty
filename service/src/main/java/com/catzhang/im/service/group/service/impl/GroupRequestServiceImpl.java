package com.catzhang.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.catzhang.im.codec.pack.group.AddGroupRequestPack;
import com.catzhang.im.codec.pack.group.ApproveGroupRequestPack;
import com.catzhang.im.codec.pack.group.ReadAllGroupRequestPack;
import com.catzhang.im.common.ClientType;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ApproveGroupRequestStatus;
import com.catzhang.im.common.enums.GroupErrorCode;
import com.catzhang.im.common.enums.GroupMemberRole;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.service.group.dao.GroupMemberEntity;
import com.catzhang.im.service.group.dao.GroupRequestEntity;
import com.catzhang.im.service.group.dao.mapper.GroupRequestMapper;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.group.service.GroupRequestService;
import com.catzhang.im.service.sequence.RedisSequence;
import com.catzhang.im.service.utils.MessageProducer;
import com.catzhang.im.service.utils.WriteUserSequence;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author crazycatzhang
 */
@Service
public class GroupRequestServiceImpl implements GroupRequestService {

    @Autowired
    GroupRequestMapper groupRequestMapper;

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    RedisSequence redisSequence;

    @Autowired
    WriteUserSequence writeUserSequence;

    @Autowired
    MessageProducer messageProducer;

    @Override
    public ResponseVO<AddGroupRequestResp> addGroupRequest(AddGroupRequestReq req) {

        GroupDto groupItem = req.getGroupItem();

        LambdaQueryWrapper<GroupRequestEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupRequestEntity::getAppId, req.getAppId())
                .eq(GroupRequestEntity::getFromId, req.getFromId())
                .eq(GroupRequestEntity::getToGroupId, groupItem.getToGroupId());
        GroupRequestEntity groupRequestEntity = groupRequestMapper.selectOne(lambdaQueryWrapper);

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.GROUPREQUEST);

        if (groupRequestEntity == null) {
            groupRequestEntity = new GroupRequestEntity();
            groupRequestEntity.setAppId(req.getAppId());
            groupRequestEntity.setFromId(req.getFromId());
            groupRequestEntity.setSequence(sequence);
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
            groupRequestEntity.setSequence(sequence);
            groupRequestEntity.setUpdateTime(System.currentTimeMillis());
            int update = groupRequestMapper.update(groupRequestEntity, lambdaQueryWrapper);
            if (update != 1) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_REQUEST_IS_FAILED);
            }
        }

        GetGroupManagerReq getGroupManagerReq = new GetGroupManagerReq();
        getGroupManagerReq.setGroupId(req.getGroupItem().getToGroupId());
        getGroupManagerReq.setAppId(req.getAppId());
        List<GroupMemberDto> groupManager = groupMemberService.getGroupManager(getGroupManagerReq);

        //TODO: 添加群申请TCP通知
        AddGroupRequestPack addGroupRequestPack = new AddGroupRequestPack();
        BeanUtils.copyProperties(groupRequestEntity, addGroupRequestPack);
        for (GroupMemberDto groupMemberDto : groupManager) {
            messageProducer.sendToUser(groupMemberDto.getMemberId(), GroupEventCommand.JOIN_GROUP, addGroupRequestPack, req.getAppId());
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

        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.GROUPREQUEST);

        groupRequestEntity.setApproveStatus(req.getStatus());
        groupRequestEntity.setUpdateTime(System.currentTimeMillis());
        groupRequestEntity.setSequence(sequence);
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

        //TODO: 审批群申请通知
        ApproveGroupRequestPack approveGroupRequestPack = new ApproveGroupRequestPack();
        approveGroupRequestPack.setId(req.getId());
        approveGroupRequestPack.setStatus(req.getStatus());
        approveGroupRequestPack.setSequence(sequence);
        for (String managerId : managerIds) {
            if (req.getClientType() != null && req.getClientType() != ClientType.WEBAPI.getCode() && Objects.equals(managerId, req.getOperator())) {
                messageProducer.sendToUserExceptClient(managerId, GroupEventCommand.GROUP_REQUEST_APPROVE, approveGroupRequestPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
            } else {
                messageProducer.sendToUser(managerId, GroupEventCommand.GROUP_REQUEST_APPROVE, approveGroupRequestPack, req.getAppId());
            }
        }

        return ResponseVO.successResponse(new ApproveGroupRequestResp(groupRequestEntity, null));
    }

    @Override
    public ResponseVO<ReadGroupRequestResp> readGroupRequest(ReadGroupRequestReq req) {
        long sequence = redisSequence.getSequence(req.getAppId() + ":" + Constants.SequenceConstants.GROUPREQUEST);

        GetGroupManagerReq getGroupManagerReq = new GetGroupManagerReq();
        getGroupManagerReq.setAppId(req.getAppId());
        getGroupManagerReq.setGroupId(req.getGroupId());
        List<GroupMemberDto> groupManager = groupMemberService.getGroupManager(getGroupManagerReq);
        List<String> managerIds = new ArrayList<>();
        groupManager.forEach(item -> {
            managerIds.add(item.getMemberId());
        });
        if (!managerIds.contains(req.getOperator())) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
        }

        LambdaUpdateWrapper<GroupRequestEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(GroupRequestEntity::getAppId, req.getAppId())
                .eq(GroupRequestEntity::getToGroupId, req.getGroupId())
                .set(GroupRequestEntity::getReadStatus, 1)
                .set(GroupRequestEntity::getSequence, sequence);

        groupRequestMapper.update(null, lambdaUpdateWrapper);

        GetGroupRequestReq getGroupRequestReq = new GetGroupRequestReq();
        BeanUtils.copyProperties(req, getGroupRequestReq);
        ResponseVO<GetGroupRequestResp> groupRequest = this.getGroupRequest(getGroupRequestReq);
        if (!groupRequest.isOk()) {
            return ResponseVO.errorResponse(groupRequest.getCode(), groupRequest.getMsg());
        }

        //TODO: 已读所有群申请通知
        ReadAllGroupRequestPack readAllGroupRequestPack = new ReadAllGroupRequestPack();
        readAllGroupRequestPack.setFromId(req.getOperator());
        readAllGroupRequestPack.setSequence(sequence);
        messageProducer.sendToUser(req.getOperator(), req.getClientType(), req.getImei(), GroupEventCommand.GROUP_REQUEST_READ, readAllGroupRequestPack, req.getAppId());

        return ResponseVO.successResponse(new ReadGroupRequestResp(groupRequest.getData().getGroupRequestEntityList()));
    }

    @Override
    public ResponseVO<GetGroupRequestResp> getGroupRequest(GetGroupRequestReq req) {
        LambdaQueryWrapper<GroupRequestEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupRequestEntity::getAppId, req.getAppId())
                .eq(GroupRequestEntity::getToGroupId, req.getGroupId());
        List<GroupRequestEntity> groupRequestEntities = groupRequestMapper.selectList(lambdaQueryWrapper);
        if (groupRequestEntities.size() == 0) {
            return ResponseVO.errorResponse(GroupErrorCode.ADD_GROUP_APPLICATION_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(new GetGroupRequestResp(groupRequestEntities));
    }
}
