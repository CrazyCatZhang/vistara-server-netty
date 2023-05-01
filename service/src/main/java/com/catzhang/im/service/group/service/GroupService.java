package com.catzhang.im.service.group.service;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
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

    ResponseVO<AddGroupResp> addGroup(AddGroupReq req);

    ResponseVO<SyncResp<GroupEntity>> syncJoinedGroupList(SyncReq req);

    Long getUserGroupMaxSequence(String userId, Integer appId);
}
