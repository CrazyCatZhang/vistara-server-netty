package com.catzhang.im.appservices.controller;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.common.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author crazycatzhang
 */
@RestController
public class CallbackController {

    private static Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @RequestMapping("/callback")
    public ResponseVO callback(@RequestBody Object req, String command, Integer appId) {
        logger.info("{}收到{}回调数据：{}",
                appId, command, JSONObject.toJSONString(req));
        return ResponseVO.successResponse(req);
    }

}
