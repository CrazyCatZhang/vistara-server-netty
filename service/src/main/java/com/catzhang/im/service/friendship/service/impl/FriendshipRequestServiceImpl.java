package com.catzhang.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.FriendShipErrorCode;
import com.catzhang.im.service.friendship.dao.FriendShipRequestEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipRequestMapper;
import com.catzhang.im.service.friendship.model.req.AddFriendShipRequestReq;
import com.catzhang.im.service.friendship.model.req.FriendDto;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipRequestResp;
import com.catzhang.im.service.friendship.service.FriendShipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author crazycatzhang
 */
@Service
public class FriendshipRequestServiceImpl implements FriendShipRequestService {

    @Autowired
    FriendShipRequestMapper friendShipRequestMapper;

    @Override
    public ResponseVO<AddFriendShipRequestResp> addFriendShipRequest(AddFriendShipRequestReq req) {
        FriendDto toItem = req.getToItem();
        LambdaQueryWrapper<FriendShipRequestEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(FriendShipRequestEntity::getAppId, req.getAppId())
                .like(FriendShipRequestEntity::getFromId, req.getFromId())
                .like(FriendShipRequestEntity::getToId, toItem.getToId());
        FriendShipRequestEntity friendShipRequest = friendShipRequestMapper.selectOne(lambdaQueryWrapper);
        if (friendShipRequest == null) {
            friendShipRequest = new FriendShipRequestEntity();
            friendShipRequest.setAppId(req.getAppId());
            friendShipRequest.setFromId(req.getFromId());
            friendShipRequest.setToId(toItem.getToId());
            friendShipRequest.setRemark(toItem.getRemark());
            friendShipRequest.setAddSource(toItem.getAddSource());
            friendShipRequest.setAddWording(toItem.getAddWording());
            friendShipRequest.setReadStatus(0);
            friendShipRequest.setApproveStatus(0);
            friendShipRequest.setCreateTime(System.currentTimeMillis());
            int insert = friendShipRequestMapper.insert(friendShipRequest);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_REQUEST_IS_FAILED);
            }
        } else {
            if (StringUtils.isNotBlank(toItem.getAddSource())) {
                friendShipRequest.setAddWording(toItem.getAddWording());
            }
            if (StringUtils.isNotBlank(toItem.getRemark())) {
                friendShipRequest.setRemark(toItem.getRemark());
            }
            if (StringUtils.isNotBlank(toItem.getAddWording())) {
                friendShipRequest.setAddWording(toItem.getAddWording());
            }
            friendShipRequest.setReadStatus(0);
            friendShipRequest.setApproveStatus(0);
            friendShipRequest.setUpdateTime(System.currentTimeMillis());
            int update = friendShipRequestMapper.update(friendShipRequest, lambdaQueryWrapper);
            if (update != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_REQUEST_IS_FAILED);
            }
        }
        return ResponseVO.successResponse(new AddFriendShipRequestResp(friendShipRequest));
    }
}
