package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddGroupRequestReq;
import com.catzhang.im.service.group.model.req.ApproveGroupRequestReq;
import com.catzhang.im.service.group.model.req.GetGroupRequestReq;
import com.catzhang.im.service.group.model.req.ReadGroupRequestReq;
import com.catzhang.im.service.group.model.resp.AddGroupRequestResp;
import com.catzhang.im.service.group.model.resp.ApproveGroupRequestResp;
import com.catzhang.im.service.group.model.resp.GetGroupRequestResp;
import com.catzhang.im.service.group.model.resp.ReadGroupRequestResp;

/**
 * @author crazycatzhang
 */
public interface GroupRequestService {


    ResponseVO<AddGroupRequestResp> addGroupRequest(AddGroupRequestReq req);

    ResponseVO<ApproveGroupRequestResp> approveGroupRequest(ApproveGroupRequestReq req);

    ResponseVO<ReadGroupRequestResp> readGroupRequest(ReadGroupRequestReq req);

    ResponseVO<GetGroupRequestResp> getGroupRequest(GetGroupRequestReq req);
}
