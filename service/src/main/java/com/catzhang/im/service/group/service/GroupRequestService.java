package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddGroupRequestReq;
import com.catzhang.im.service.group.model.req.ApproveGroupRequestReq;
import com.catzhang.im.service.group.model.resp.AddGroupRequestResp;
import com.catzhang.im.service.group.model.resp.ApproveGroupRequestResp;

/**
 * @author crazycatzhang
 */
public interface GroupRequestService {


    ResponseVO<AddGroupRequestResp> addGroupRequest(AddGroupRequestReq req);

    ResponseVO<ApproveGroupRequestResp> approveGroupRequest(ApproveGroupRequestReq req);
}
