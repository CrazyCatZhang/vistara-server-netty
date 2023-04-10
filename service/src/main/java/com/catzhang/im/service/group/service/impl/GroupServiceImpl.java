package com.catzhang.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.GroupErrorCode;
import com.catzhang.im.common.enums.GroupStatus;
import com.catzhang.im.common.enums.GroupType;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.service.group.dao.GroupEntity;
import com.catzhang.im.service.group.dao.mapper.GroupMapper;
import com.catzhang.im.service.group.model.req.ImportGroupReq;
import com.catzhang.im.service.group.model.resp.ImportGroupResp;
import com.catzhang.im.service.group.service.GroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author crazycatzhang
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    GroupMapper groupMapper;

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
}
