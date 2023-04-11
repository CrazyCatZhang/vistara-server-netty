package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.AddGroupMemberResp;
import com.catzhang.im.service.group.model.resp.GetRoleInGroupResp;
import com.catzhang.im.service.group.model.resp.ImportGroupMemberResp;
import com.catzhang.im.service.group.model.resp.TransferGroupMemberResp;

import java.util.Collection;
import java.util.List;

/**
 * @author crazycatzhang
 */
public interface GroupMemberService {

    ResponseVO<List<ImportGroupMemberResp>> importGroupMember(ImportGroupMemberReq req);

    ResponseVO<AddGroupMemberResp> addGroupMember(AddGroupMemberReq req);

    ResponseVO<GetRoleInGroupResp> getRoleInGroup(GetRoleInGroupReq req);

    ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);

    ResponseVO<TransferGroupMemberResp> transferGroupMember(TransferGroupMemberReq req);

    ResponseVO<List<GroupMemberDto>> getGroupMember(GetGroupMemberReq req);
}
