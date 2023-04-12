package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddGroupRequestReq;
import com.catzhang.im.service.group.model.resp.AddGroupRequestResp;

/**
 * @author crazycatzhang
 */
public interface GroupRequestService {

    ResponseVO<AddGroupRequestResp> addGroupRequest(AddGroupRequestReq req);

}
