package com.catzhang.im.service.message.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.model.message.VerifySendMessageReq;
import com.catzhang.im.service.group.service.GroupMessageService;
import com.catzhang.im.service.message.model.req.SendMessageReq;
import com.catzhang.im.service.message.model.resp.SendMessageResp;
import com.catzhang.im.service.message.service.P2PMessageService;
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
@RequestMapping("/message")
public class MessageController {

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    GroupMessageService groupMessageService;

    @PostMapping("/send")
    public ResponseVO<SendMessageResp> send(@RequestBody @Validated SendMessageReq req) {
        return p2PMessageService.send(req);
    }

    @PostMapping("/verifySend")
    public ResponseVO verifySend(@RequestBody @Validated VerifySendMessageReq req) {
        if (req.getCommand().equals(GroupEventCommand.MSG_GROUP.getCommand())) {
            return groupMessageService.verifyImServerPermission(req.getFromId(), req.getToId(), req.getAppId());
        }
        return p2PMessageService.verifyImServerPermission(req.getFromId(), req.getToId(), req.getAppId());
    }

}
