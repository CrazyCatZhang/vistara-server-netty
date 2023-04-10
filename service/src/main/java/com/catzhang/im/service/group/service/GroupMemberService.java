package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddGroupMemberReq;
import com.catzhang.im.service.group.model.resp.AddGroupMemberResp;

/**
 * @author crazycatzhang
 */
public interface GroupMemberService {

    ResponseVO<AddGroupMemberResp> addGroupMember(AddGroupMemberReq req);

}
