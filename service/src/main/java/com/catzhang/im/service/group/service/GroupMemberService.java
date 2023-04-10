package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddGroupMemberReq;
import com.catzhang.im.service.group.model.req.GetJoinedGroupReq;
import com.catzhang.im.service.group.model.req.GetRoleInGroupReq;
import com.catzhang.im.service.group.model.resp.AddGroupMemberResp;
import com.catzhang.im.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;

/**
 * @author crazycatzhang
 */
public interface GroupMemberService {

    ResponseVO<AddGroupMemberResp> addGroupMember(AddGroupMemberReq req);

    ResponseVO<GetRoleInGroupResp> getRoleInGroup(GetRoleInGroupReq req);

    ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);
}
