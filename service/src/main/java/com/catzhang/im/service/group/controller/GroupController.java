package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
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
    public ResponseVO<ImportGroupResp> importGroup(@RequestBody @Validated ImportGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.importGroup(req);
    }

    @PostMapping("create")
    public ResponseVO<CreateGroupResp> createGroup(@RequestBody @Validated CreateGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.createGroup(req);
    }

    @PutMapping("update")
    public ResponseVO<UpdateGroupInfoResp> updateGroupInfo(@RequestBody @Validated UpdateGroupInfoReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.updateGroupInfo(req);
    }

    @PutMapping("destroy")
    public ResponseVO<DestroyGroupResp> destroyGroup(@RequestBody @Validated DestroyGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.destroyGroup(req);
    }

    @PutMapping("transfer")
    public ResponseVO<TransferGroupResp> transferGroup(@RequestBody @Validated TransferGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.transferGroup(req);
    }

    @GetMapping("get")
    public ResponseVO<GetGroupResp> getGroup(@RequestBody @Validated GetGroupReq req, Integer appId) {
        req.setAppId(appId);
        return groupService.getGroup(req);
    }

    @PutMapping("mute")
    public ResponseVO<MuteGroupResp> muteGroup(@RequestBody @Validated MuteGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.muteGroup(req);
    }

    @GetMapping("getJoined")
    public ResponseVO<GetJoinedGroupResp> getJoinedGroup(@RequestBody @Validated GetJoinedGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.getJoinedGroup(req);
    }

    @PostMapping("add")
    public ResponseVO<AddGroupResp> addGroup(@RequestBody @Validated AddGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.addGroup(req);
    }

    @PostMapping("sendMessage")
    public ResponseVO<SendGroupMessageResp> sendMessage(@RequestBody @Validated SendGroupMessageReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMessageService.send(req);
    }

    @GetMapping("syncJoinedGroup")
    public ResponseVO<SyncResp<GroupEntity>> syncJoinedGroup(@RequestBody @Validated SyncReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.syncJoinedGroupList(req);
    }
}
