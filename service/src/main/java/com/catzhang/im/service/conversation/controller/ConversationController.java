package com.catzhang.im.service.conversation.controller;


import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.model.SyncReq;
import com.catzhang.im.common.model.SyncResp;
import com.catzhang.im.service.conversation.dao.ConversationSetEntity;
import com.catzhang.im.service.conversation.model.DeleteConversationReq;
import com.catzhang.im.service.conversation.model.UpdateConversationReq;
import com.catzhang.im.service.conversation.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    ConversationService conversationService;

    @RequestMapping("/deleteConversation")
    public ResponseVO deleteConversation(@RequestBody @Validated DeleteConversationReq req, Integer appId) {
        req.setAppId(appId);
        return conversationService.deleteConversation(req);
    }

    @RequestMapping("/updateConversation")
    public ResponseVO updateConversation(@RequestBody @Validated UpdateConversationReq req, Integer appId) {
        req.setAppId(appId);
        return conversationService.updateConversation(req);
    }

    @RequestMapping("syncConversationList")
    public ResponseVO<SyncResp<ConversationSetEntity>> syncConversationList(@RequestBody @Validated SyncReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return conversationService.syncConversationSet(req);
    }

}
