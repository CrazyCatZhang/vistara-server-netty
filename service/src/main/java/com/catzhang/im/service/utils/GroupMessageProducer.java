package com.catzhang.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.codec.pack.group.AddGroupMemberPack;
import com.catzhang.im.codec.pack.group.ExitGroupPack;
import com.catzhang.im.codec.pack.group.RemoveGroupMemberPack;
import com.catzhang.im.codec.pack.group.UpdateGroupMemberPack;
import com.catzhang.im.common.ClientType;
import com.catzhang.im.common.enums.command.Command;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.service.group.model.req.GetGroupManagerReq;
import com.catzhang.im.service.group.model.req.GetGroupMemberIdReq;
import com.catzhang.im.service.group.model.req.GroupMemberDto;
import com.catzhang.im.service.group.service.GroupMemberService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author crazycatzhang
 */
@Component
public class GroupMessageProducer {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    GroupMemberService groupMemberService;

    public void producer(String userId, Command command, Object data, ClientInfo clientInfo) {

        JSONObject json = (JSONObject) JSONObject.toJSON(data);

        String groupId = json.getString("groupId");

        GetGroupMemberIdReq getGroupMemberIdReq = new GetGroupMemberIdReq();
        getGroupMemberIdReq.setAppId(clientInfo.getAppId());
        getGroupMemberIdReq.setGroupId(groupId);

        GetGroupManagerReq getGroupManagerReq = new GetGroupManagerReq();
        BeanUtils.copyProperties(getGroupMemberIdReq, getGroupManagerReq);

        List<String> groupMemberId = groupMemberService.getGroupMemberId(getGroupMemberIdReq);

        if (command.equals(GroupEventCommand.ADDED_MEMBER)) {
            //发送给管理员和被加入人本身
            List<GroupMemberDto> groupManager = groupMemberService.getGroupManager(getGroupManagerReq);
            AddGroupMemberPack addGroupMemberPack = json.toJavaObject(AddGroupMemberPack.class);
            List<String> members = addGroupMemberPack.getMembers();
            groupManager.forEach(manager -> {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && manager.getMemberId().equals(userId)) {
                    messageProducer.sendToUserExceptClient(manager.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(manager.getMemberId(), command, data, clientInfo.getAppId());
                }
            });
            members.forEach(member -> {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    messageProducer.sendToUserExceptClient(member, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(member, command, data, clientInfo.getAppId());
                }
            });
        } else if (command.equals(GroupEventCommand.DELETED_MEMBER)) {
            //发送给群内所有成员以及被踢成员本身
            RemoveGroupMemberPack pack = json.toJavaObject(RemoveGroupMemberPack.class);
            String member = pack.getMember();
            List<String> members = groupMemberService.getGroupMemberId(getGroupMemberIdReq);
            members.add(member);
            for (String memberId : members) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && memberId.equals(userId)) {
                    messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.UPDATED_MEMBER)) {
            UpdateGroupMemberPack pack =
                    json.toJavaObject(UpdateGroupMemberPack.class);
            String memberId = pack.getMemberId();
            List<GroupMemberDto> groupManager = groupMemberService.getGroupManager(getGroupManagerReq);
            GroupMemberDto groupMemberDto = new GroupMemberDto();
            groupMemberDto.setMemberId(memberId);
            groupManager.add(groupMemberDto);
            for (GroupMemberDto member : groupManager) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.getMemberId().equals(userId)) {
                    messageProducer.sendToUserExceptClient(member.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(member.getMemberId(), command, data, clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.EXIT_GROUP)) {
            //发送给管理员和退出人本身
            List<GroupMemberDto> groupManager = groupMemberService.getGroupManager(getGroupManagerReq);
            ExitGroupPack javaObject = json.toJavaObject(ExitGroupPack.class);
            String exitMemberId = javaObject.getUserId();
            GroupMemberDto groupMemberDto = new GroupMemberDto();
            groupMemberDto.setMemberId(exitMemberId);
            groupManager.add(groupMemberDto);
            groupManager.forEach(manager -> {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && manager.getMemberId().equals(userId)) {
                    messageProducer.sendToUserExceptClient(manager.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(manager.getMemberId(), command, data, clientInfo.getAppId());
                }
            });
        } else {
            groupMemberId.forEach(memberId -> {
                if (clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() && Objects.equals(memberId, userId)) {
                    messageProducer.sendToUserExceptClient(userId, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
                }
            });
        }

    }

}
