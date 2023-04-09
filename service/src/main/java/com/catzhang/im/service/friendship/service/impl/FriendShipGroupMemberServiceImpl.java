package com.catzhang.im.service.friendship.service.impl;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.friendship.dao.FriendShipGroupMemberEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipGroupMemberMapper;
import com.catzhang.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.catzhang.im.service.friendship.model.req.GetFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.req.HandleAddFriendShipGroupMemberReq;
import com.catzhang.im.service.friendship.model.resp.AddFriendShipGroupMemberResp;
import com.catzhang.im.service.friendship.model.resp.GetFriendShipGroupResp;
import com.catzhang.im.service.friendship.model.resp.HandleAddFriendShipGroupMemberResp;
import com.catzhang.im.service.friendship.service.FriendShipGroupMemberService;
import com.catzhang.im.service.friendship.service.FriendShipGroupService;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class FriendShipGroupMemberServiceImpl implements FriendShipGroupMemberService {

    @Autowired
    FriendShipGroupMemberMapper friendShipGroupMemberMapper;

    @Autowired
    FriendShipGroupService friendShipGroupService;

    @Autowired
    UserService userService;

    @Override
    @Transactional
    public ResponseVO<AddFriendShipGroupMemberResp> addFriendShipGroupMember(AddFriendShipGroupMemberReq req) {
        GetFriendShipGroupReq getFriendShipGroupReq = new GetFriendShipGroupReq();
        getFriendShipGroupReq.setAppId(req.getAppId());
        getFriendShipGroupReq.setGroupName(req.getGroupName());
        getFriendShipGroupReq.setFromId(req.getFromId());
        ResponseVO<GetFriendShipGroupResp> friendShipGroup = friendShipGroupService.getFriendShipGroup(getFriendShipGroupReq);
        if (!friendShipGroup.isOk()) {
            return ResponseVO.errorResponse(friendShipGroup.getCode(), friendShipGroup.getMsg());
        }

        List<String> successIds = new ArrayList<>();
        List<String> failureIds = new ArrayList<>();

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setAppId(req.getAppId());
        HandleAddFriendShipGroupMemberReq handleAddFriendShipGroupMemberReq = new HandleAddFriendShipGroupMemberReq();
        handleAddFriendShipGroupMemberReq.setGroupId(friendShipGroup.getData().getFriendShipGroupEntity().getGroupId());

        for (String toId :
                req.getToIds()) {
            getSingleUserInfoReq.setUserId(toId);
            handleAddFriendShipGroupMemberReq.setToId(toId);
            ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
            if (singleUserInfo.isOk()) {
                ResponseVO<HandleAddFriendShipGroupMemberResp> handleAddFriendShipGroupMemberRespResponseVO = this.handleAddFriendShipGroupMember(handleAddFriendShipGroupMemberReq);
                if (!handleAddFriendShipGroupMemberRespResponseVO.isOk()) {
                    failureIds.add(toId);
                    continue;
                }
                if (handleAddFriendShipGroupMemberRespResponseVO.getData().getResult() == 1) {
                    successIds.add(toId);
                }
            } else {
                return ResponseVO.errorResponse(singleUserInfo.getCode(), toId + singleUserInfo.getMsg());
            }
        }
        return ResponseVO.successResponse(new AddFriendShipGroupMemberResp(successIds, failureIds));
    }

    @Override
    public ResponseVO<HandleAddFriendShipGroupMemberResp> handleAddFriendShipGroupMember(HandleAddFriendShipGroupMemberReq req) {
        FriendShipGroupMemberEntity friendShipGroupMemberEntity = new FriendShipGroupMemberEntity();
        friendShipGroupMemberEntity.setGroupId(req.getGroupId());
        friendShipGroupMemberEntity.setToId(req.getToId());
        try {
            int insert = friendShipGroupMemberMapper.insert(friendShipGroupMemberEntity);
            return ResponseVO.successResponse(new HandleAddFriendShipGroupMemberResp(insert));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.errorResponse();
        }
    }
}
