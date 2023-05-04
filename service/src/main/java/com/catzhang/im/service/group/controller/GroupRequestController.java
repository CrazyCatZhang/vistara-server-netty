package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddGroupRequestReq;
import com.catzhang.im.service.group.model.req.ApproveGroupRequestReq;
import com.catzhang.im.service.group.model.req.GetGroupRequestReq;
import com.catzhang.im.service.group.model.req.ReadGroupRequestReq;
import com.catzhang.im.service.group.model.resp.AddGroupRequestResp;
import com.catzhang.im.service.group.model.resp.ApproveGroupRequestResp;
import com.catzhang.im.service.group.model.resp.GetGroupRequestResp;
import com.catzhang.im.service.group.model.resp.ReadGroupRequestResp;
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

    @RequestMapping("add")
    public ResponseVO<AddGroupRequestResp> addGroupRequest(@RequestBody @Validated AddGroupRequestReq req, Integer appId) {
        req.setAppId(appId);
        return groupRequestService.addGroupRequest(req);
    }

    @RequestMapping("approve")
    public ResponseVO<ApproveGroupRequestResp> approveGroupRequest(@RequestBody @Validated ApproveGroupRequestReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupRequestService.approveGroupRequest(req);
    }

    @RequestMapping("read")
    public ResponseVO<ReadGroupRequestResp> readGroupRequest(@RequestBody @Validated ReadGroupRequestReq req, Integer appId) {
        req.setAppId(appId);
        return groupRequestService.readGroupRequest(req);
    }

    @RequestMapping("get")
    public ResponseVO<GetGroupRequestResp> getGroupRequest(@RequestBody @Validated GetGroupRequestReq req, Integer appId) {
        req.setAppId(appId);
        return groupRequestService.getGroupRequest(req);
    }

    //TODO: 同步最大的群申请Sequence

}
