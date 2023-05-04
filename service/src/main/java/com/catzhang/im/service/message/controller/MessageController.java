package com.catzhang.im.service.message.controller;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.command.GroupEventCommand;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
import com.catzhang.im.common.model.message.OfflineMessageContent;
import com.catzhang.im.common.model.message.VerifySendMessageReq;
import com.catzhang.im.service.group.service.GroupMessageService;
import com.catzhang.im.service.message.model.req.SendMessageReq;
import com.catzhang.im.service.message.model.resp.SendMessageResp;
import com.catzhang.im.service.message.service.MessageSyncService;
import com.catzhang.im.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    MessageSyncService messageSyncService;

    @RequestMapping("/send")
    public ResponseVO<SendMessageResp> send(@RequestBody @Validated SendMessageReq req, Integer appId) {
        req.setAppId(appId);
        return p2PMessageService.send(req);
    }

    @RequestMapping("/verifySend")
    public ResponseVO verifySend(@RequestBody @Validated VerifySendMessageReq req, Integer appId) {
        req.setAppId(appId);
        if (req.getCommand().equals(GroupEventCommand.MSG_GROUP.getCommand())) {
            return groupMessageService.verifyImServerPermission(req.getFromId(), req.getToId(), req.getAppId());
        }
        return p2PMessageService.verifyImServerPermission(req.getFromId(), req.getToId(), req.getAppId());
    }

    @RequestMapping("syncOfflineMessage")
    public ResponseVO<SyncResp<OfflineMessageContent>> syncOfflineMessage(@RequestBody @Validated SyncReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return messageSyncService.syncOfflineMessage(req);
    }

}
