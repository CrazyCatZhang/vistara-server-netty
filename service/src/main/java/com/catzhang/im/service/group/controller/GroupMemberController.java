package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.*;
import com.catzhang.im.service.group.model.resp.*;
import com.catzhang.im.service.group.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/group/member")
public class GroupMemberController {

    @Autowired
    GroupMemberService groupMemberService;

    @RequestMapping("import")
    public ResponseVO<List<ImportGroupMemberResp>> importGroupMember(@RequestBody @Validated ImportGroupMemberReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMemberService.importGroupMember(req);
    }

    @RequestMapping("add")
    public ResponseVO<List<AddMemberResp>> addMember(@RequestBody @Validated AddMemberReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMemberService.addMember(req);
    }

    @RequestMapping("remove")
    public ResponseVO<RemoveMemberResp> removeMember(@RequestBody @Validated RemoveMemberReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMemberService.removeMember(req);
    }

    @RequestMapping("exit")
    public ResponseVO<ExitGroupResp> exitGroup(@RequestBody @Validated ExitGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMemberService.exitGroup(req);
    }

    @RequestMapping("update")
    public ResponseVO<UpdateGroupMemberResp> updateGroupMember(@RequestBody @Validated UpdateGroupMemberReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMemberService.updateGroupMember(req);
    }

    @RequestMapping("speak")
    public ResponseVO<SpeakMemberResp> speakMember(@RequestBody @Validated SpeakMemberReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMemberService.speakMember(req);
    }

    //TODO: 同步最大的群成员Sequence

}
