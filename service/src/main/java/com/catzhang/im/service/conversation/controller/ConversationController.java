package com.catzhang.im.service.conversation.controller;


import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.service.conversation.model.DeleteConversationReq;
import com.catzhang.im.service.conversation.model.UpdateConversationReq;
import com.catzhang.im.service.conversation.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    ConversationService conversationService;

    @RequestMapping("/deleteConversation")
    public ResponseVO deleteConversation(@RequestBody @Validated DeleteConversationReq req) {
        return conversationService.deleteConversation(req);
    }

    @RequestMapping("/updateConversation")
    public ResponseVO updateConversation(@RequestBody @Validated UpdateConversationReq
                                                 req) {
        return conversationService.updateConversation(req);
    }

}
