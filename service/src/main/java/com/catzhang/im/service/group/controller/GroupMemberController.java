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

    @PostMapping("import")
    public ResponseVO<List<ImportGroupMemberResp>> importGroupMember(@RequestBody @Validated ImportGroupMemberReq req) {
        return groupMemberService.importGroupMember(req);
    }

    @PostMapping("add")
    public ResponseVO<List<AddMemberResp>> addMember(@RequestBody @Validated AddMemberReq req) {
        return groupMemberService.addMember(req);
    }

    @DeleteMapping("remove")
    public ResponseVO<RemoveMemberResp> removeMember(@RequestBody @Validated RemoveMemberReq req) {
        return groupMemberService.removeMember(req);
    }

    @DeleteMapping("exit")
    public ResponseVO<ExitGroupResp> exitGroup(@RequestBody @Validated ExitGroupReq req) {
        return groupMemberService.exitGroup(req);
    }

    @PutMapping("update")
    public ResponseVO<UpdateGroupMemberResp> updateGroupMember(@RequestBody @Validated UpdateGroupMemberReq req) {
        return groupMemberService.updateGroupMember(req);
    }

    @PutMapping("speak")
    public ResponseVO<SpeakMemberResp> speakMember(@RequestBody @Validated SpeakMemberReq req) {
        return groupMemberService.speakMember(req);
    }

}
