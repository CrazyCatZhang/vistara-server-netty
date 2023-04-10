package com.catzhang.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.GroupErrorCode;
import com.catzhang.im.common.enums.GroupMemberRole;
import com.catzhang.im.service.group.dao.GroupMemberEntity;
import com.catzhang.im.service.group.dao.mapper.GroupMemberMapper;
import com.catzhang.im.service.group.model.req.AddGroupMemberReq;
import com.catzhang.im.service.group.model.req.GetJoinedGroupReq;
import com.catzhang.im.service.group.model.req.GetRoleInGroupReq;
import com.catzhang.im.service.group.model.req.GroupMemberDto;
import com.catzhang.im.service.group.model.resp.AddGroupMemberResp;
import com.catzhang.im.service.group.model.resp.GetRoleInGroupResp;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
