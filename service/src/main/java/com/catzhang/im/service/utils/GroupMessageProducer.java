package com.catzhang.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.common.ClientType;
import com.catzhang.im.common.enums.command.Command;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.service.group.model.req.GetGroupMemberIdReq;
import com.catzhang.im.service.group.service.GroupMemberService;
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

        List<String> groupMemberId = groupMemberService.getGroupMemberId(getGroupMemberIdReq);

        groupMemberId.forEach(memberId -> {
            if (clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() && Objects.equals(memberId, userId)) {
                messageProducer.sendToUserExceptClient(userId, command, data, clientInfo);
            } else {
                messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
            }
        });

    }

}
