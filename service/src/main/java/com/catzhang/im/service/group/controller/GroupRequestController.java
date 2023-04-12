package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddGroupRequestReq;
import com.catzhang.im.service.group.model.req.ApproveGroupRequestReq;
import com.catzhang.im.service.group.model.resp.AddGroupRequestResp;
import com.catzhang.im.service.group.model.resp.ApproveGroupRequestResp;
import com.catzhang.im.service.group.service.GroupRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author crazycatzhang
 */
@RestController
@RequestMapping("/group/request")
public class GroupRequestController {

    @Autowired
    GroupRequestService groupRequestService;

    @PostMapping("add")
    public ResponseVO<AddGroupRequestResp> addGroupRequest(@RequestBody @Validated AddGroupRequestReq req) {
        return groupRequestService.addGroupRequest(req);
    }

    @PutMapping("approve")
    public ResponseVO<ApproveGroupRequestResp> approveGroupRequest(@RequestBody @Validated ApproveGroupRequestReq req) {
        return groupRequestService.approveGroupRequest(req);
    }

}
