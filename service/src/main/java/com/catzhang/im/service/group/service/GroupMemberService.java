package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;

import java.util.Collection;
import java.util.List;

/**
 * @author crazycatzhang
 */
public interface GroupMemberService {

    ResponseVO<List<ImportGroupMemberResp>> importGroupMember(ImportGroupMemberReq req);

    ResponseVO<List<AddMemberResp>> addMember(AddMemberReq req);

    ResponseVO<AddGroupMemberResp> addGroupMember(AddGroupMemberReq req);

    ResponseVO<GetRoleInGroupResp> getRoleInGroup(GetRoleInGroupReq req);

    ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);

    List<String> getGroupMemberId(GetGroupMemberIdReq req);

    ResponseVO<TransferGroupMemberResp> transferGroupMember(TransferGroupMemberReq req);

    ResponseVO<List<GroupMemberDto>> getGroupMember(GetGroupMemberReq req);

    ResponseVO<RemoveMemberResp> removeMember(RemoveMemberReq req);

    ResponseVO<RemoveMemberResp> removeGroupMember(RemoveMemberReq req);

    ResponseVO<ExitGroupResp> exitGroup(ExitGroupReq req);
}
