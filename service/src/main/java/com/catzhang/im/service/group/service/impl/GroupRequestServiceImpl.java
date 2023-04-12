package com.catzhang.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.GroupErrorCode;
import com.catzhang.im.service.group.dao.GroupRequestEntity;
import com.catzhang.im.service.group.dao.mapper.GroupRequestMapper;
import com.catzhang.im.service.group.model.req.AddGroupRequestReq;
import com.catzhang.im.service.group.model.req.GroupDto;
import com.catzhang.im.service.group.model.resp.AddGroupRequestResp;
import com.catzhang.im.service.group.service.GroupRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author crazycatzhang
 */
@Service
public class GroupRequestServiceImpl implements GroupRequestService {

    @Autowired
    GroupRequestMapper groupRequestMapper;

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
}
