package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.dao.GroupEntity;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;

/**
 * @author crazycatzhang
 */
public interface GroupService {

    ResponseVO<ImportGroupResp> importGroup(ImportGroupReq req);

    ResponseVO<CreateGroupResp> createGroup(CreateGroupReq req);

    ResponseVO<UpdateGroupInfoResp> updateGroupInfo(UpdateGroupInfoReq req);

    ResponseVO<GetJoinedGroupResp> getJoinedGroup(GetJoinedGroupReq req);

    ResponseVO<DestroyGroupResp> destroyGroup(DestroyGroupReq req);

    ResponseVO<TransferGroupResp> transferGroup(TransferGroupReq req);

    ResponseVO<GetGroupResp> getGroup(GetGroupReq req);

    ResponseVO<GroupEntity> handleGetGroup(GetGroupReq req);

    ResponseVO<MuteGroupResp> muteGroup(MuteGroupReq req);
}
