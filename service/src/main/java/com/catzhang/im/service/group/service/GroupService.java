package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.CreateGroupReq;
import com.catzhang.im.service.group.model.req.ImportGroupReq;
import com.catzhang.im.service.group.model.resp.CreateGroupResp;
import com.catzhang.im.service.group.model.resp.ImportGroupResp;

/**
 * @author crazycatzhang
 */
public interface GroupService {

    ResponseVO<ImportGroupResp> importGroup(ImportGroupReq req);

    ResponseVO<CreateGroupResp> createGroup(CreateGroupReq req);
}
