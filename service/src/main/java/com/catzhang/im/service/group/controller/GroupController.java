package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.CreateGroupReq;
import com.catzhang.im.service.group.model.req.ImportGroupReq;
import com.catzhang.im.service.group.model.req.UpdateGroupInfoReq;
import com.catzhang.im.service.group.model.resp.CreateGroupResp;
import com.catzhang.im.service.group.model.resp.ImportGroupResp;
import com.catzhang.im.service.group.model.resp.UpdateGroupInfoResp;
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
}
