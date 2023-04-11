package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddMemberReq;
import com.catzhang.im.service.group.model.req.ImportGroupMemberReq;
import com.catzhang.im.service.group.model.req.RemoveMemberReq;
import com.catzhang.im.service.group.model.resp.AddMemberResp;
import com.catzhang.im.service.group.model.resp.ImportGroupMemberResp;
import com.catzhang.im.service.group.model.resp.RemoveMemberResp;
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

}
