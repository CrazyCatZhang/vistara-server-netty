package com.catzhang.im.service.group.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.group.model.req.AddGroupRequestReq;
import com.catzhang.im.service.group.model.resp.AddGroupRequestResp;
import com.catzhang.im.service.group.service.GroupRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
