package com.catzhang.im.service.message.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.enums.*;
import com.catzhang.im.service.friendship.dao.FriendShipEntity;
import com.catzhang.im.service.friendship.model.req.GetRelationReq;
import com.catzhang.im.service.friendship.model.resp.GetRelationResp;
import com.catzhang.im.service.friendship.service.FriendShipService;
import com.catzhang.im.service.group.model.req.GetGroupReq;
import com.catzhang.im.service.group.model.req.GetRoleInGroupReq;
import com.catzhang.im.service.group.model.resp.GetGroupResp;
import com.catzhang.im.service.group.model.resp.GetRoleInGroupResp;
import com.catzhang.im.service.group.service.GroupMemberService;
import com.catzhang.im.service.group.service.GroupService;
import com.catzhang.im.service.user.dao.UserDataEntity;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author crazycatzhang
 */
@Service
public class VerifySendMessageService {

    @Autowired
    UserService userService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    FriendShipService friendShipService;

    @Autowired
    GroupService groupService;

    @Autowired
    GroupMemberService groupMemberService;

    public ResponseVO verifySenderForbiddenAndMuted(String fromId, Integer appId) {

        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setUserId(fromId);
        getSingleUserInfoReq.setAppId(appId);
        ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        UserDataEntity user = singleUserInfo.getData().getUserDataEntity();
        if (user.getForbiddenFlag() == UserForbiddenFlag.FORBIDDEN.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.SENDER_IS_FORBIDDEN);
        } else if (user.getSilentFlag() == UserSilentFlag.MUTE.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.SENDER_IS_MUTE);
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO verifyFriendship(String fromId, String toId, Integer appId) {

        if (appConfig.isSendMessageCheckFriend()) {

            GetRelationReq getRelationReq = new GetRelationReq();
            getRelationReq.setAppId(appId);
            getRelationReq.setFromId(fromId);
            getRelationReq.setToId(toId);

            ResponseVO<GetRelationResp> fromRelation = friendShipService.getRelation(getRelationReq);
            if (!fromRelation.isOk()) {
                return fromRelation;
            }
            FriendShipEntity fromFriendShip = fromRelation.getData().getFriendShipEntity();

            getRelationReq.setFromId(toId);
            getRelationReq.setToId(fromId);

            ResponseVO<GetRelationResp> toRelation = friendShipService.getRelation(getRelationReq);
            if (!toRelation.isOk()) {
                return toRelation;
            }
            FriendShipEntity toFriendShip = toRelation.getData().getFriendShipEntity();

            if (fromFriendShip.getStatus() == FriendShipStatus.FRIEND_STATUS_DELETE.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            if (toFriendShip.getStatus() == FriendShipStatus.FRIEND_STATUS_DELETE.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_DELETED_YOU);
            }

            if (appConfig.isSendMessageCheckBlack()) {
                if (FriendShipStatus.BLACK_STATUS_NORMAL.getCode() != fromFriendShip.getBlack()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }

                if (FriendShipStatus.BLACK_STATUS_NORMAL.getCode() != toFriendShip.getBlack()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO verifyGroupMessage(String fromId, String groupId, Integer appId) {

        ResponseVO responseVO = verifySenderForbiddenAndMuted(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }

        GetGroupReq getGroupReq = new GetGroupReq();
        GetRoleInGroupReq getRoleInGroupReq = new GetRoleInGroupReq();
        getGroupReq.setGroupId(groupId);
        getGroupReq.setAppId(appId);
        BeanUtils.copyProperties(getGroupReq, getRoleInGroupReq);

        ResponseVO<GetGroupResp> group = groupService.getGroup(getGroupReq);
        if (!group.isOk()) {
            return group;
        }

        getRoleInGroupReq.setMemberId(fromId);

        ResponseVO<GetRoleInGroupResp> roleInGroup = groupMemberService.getRoleInGroup(getRoleInGroupReq);
        if (!roleInGroup.isOk()) {
            return roleInGroup;
        }

        GetGroupResp groupData = group.getData();
        GetRoleInGroupResp roleData = roleInGroup.getData();

        if (groupData.getMute() == GroupMuteType.MUTE.getCode()
                && (roleData.getRole() != GroupMemberRole.ADMINISTRATOR.getCode() || roleData.getRole() != GroupMemberRole.OWNER.getCode())) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_GROUP_IS_MUTE);
        }

        if (roleData.getSpeakDate() != null && roleData.getSpeakDate() > System.currentTimeMillis()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }

        return ResponseVO.successResponse();

    }
}
