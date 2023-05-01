package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
import com.catzhang.im.service.friendship.dao.FriendShipEntity;
import com.catzhang.im.service.group.dao.GroupEntity;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;
import com.catzhang.im.service.group.service.GroupMessageService;
import com.catzhang.im.service.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    GroupService groupService;

    @Autowired
    GroupMessageService groupMessageService;

    @PostMapping("import")
    public ResponseVO<ImportGroupResp> importGroup(@RequestBody @Validated ImportGroupReq req) {
        return groupService.importGroup(req);
    }

    @PostMapping("create")
    public ResponseVO<CreateGroupResp> createGroup(@RequestBody @Validated CreateGroupReq req) {
        return groupService.createGroup(req);
    }

    @PutMapping("update")
    public ResponseVO<UpdateGroupInfoResp> updateGroupInfo(@RequestBody @Validated UpdateGroupInfoReq req) {
        return groupService.updateGroupInfo(req);
    }

    @PutMapping("destroy")
    public ResponseVO<DestroyGroupResp> destroyGroup(@RequestBody @Validated DestroyGroupReq req) {
        return groupService.destroyGroup(req);
    }

    @PutMapping("transfer")
    public ResponseVO<TransferGroupResp> transferGroup(@RequestBody @Validated TransferGroupReq req) {
        return groupService.transferGroup(req);
    }

    @GetMapping("get")
    public ResponseVO<GetGroupResp> getGroup(@RequestBody @Validated GetGroupReq req) {
        return groupService.getGroup(req);
    }

    @PutMapping("mute")
    public ResponseVO<MuteGroupResp> muteGroup(@RequestBody @Validated MuteGroupReq req) {
        return groupService.muteGroup(req);
    }

    @GetMapping("getJoined")
    public ResponseVO<GetJoinedGroupResp> getJoinedGroup(@RequestBody @Validated GetJoinedGroupReq req) {
        return groupService.getJoinedGroup(req);
    }

    @PostMapping("add")
    public ResponseVO<AddGroupResp> addGroup(@RequestBody @Validated AddGroupReq req) {
        return groupService.addGroup(req);
    }

    @PostMapping("sendMessage")
    public ResponseVO<SendGroupMessageResp> sendMessage(@RequestBody @Validated SendGroupMessageReq req) {
        return groupMessageService.send(req);
    }

    @PostMapping("syncJoinedGroup")
    public ResponseVO<SyncResp<GroupEntity>> verifyFriendShipBlack(@RequestBody @Validated SyncReq req) {
        return groupService.syncJoinedGroupList(req);
    }}
