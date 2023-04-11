package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.ImportGroupMemberReq;
import com.catzhang.im.service.group.model.resp.ImportGroupMemberResp;
import com.catzhang.im.service.group.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
