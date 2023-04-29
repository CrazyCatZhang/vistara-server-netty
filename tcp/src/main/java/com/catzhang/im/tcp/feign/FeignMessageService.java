package com.catzhang.im.tcp.feign;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.model.message.VerifySendMessageReq;
import feign.Headers;
import feign.RequestLine;

/**
 * @author crazycatzhang
 */
public interface FeignMessageService {

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @RequestLine("POST /message/verifySend")
    public ResponseVO verifySendMessage(VerifySendMessageReq req);

}
