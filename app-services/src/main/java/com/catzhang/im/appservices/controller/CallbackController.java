package com.catzhang.im.appservices.controller;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.appservices.common.ResponseVO;
import com.catzhang.im.appservices.config.AppConfig;
import com.catzhang.im.appservices.model.proto.MessageBody;
import com.catzhang.im.appservices.model.proto.SendMessageProto;
import com.catzhang.im.appservices.utils.HttpRequestUtils;
import com.catzhang.im.appservices.utils.SignApi;
import com.plexpt.chatgpt.ChatGPTStream;
import com.plexpt.chatgpt.entity.chat.Message;
import com.plexpt.chatgpt.listener.SseStreamListener;
import com.plexpt.chatgpt.util.Proxys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.Proxy;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author crazycatzhang
 */
@RestController
public class CallbackController {

    @Autowired
    AppConfig appConfig;

    @Autowired
    HttpRequestUtils httpRequestUtils;

    private static Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @RequestMapping("/callback")
    public ResponseVO callback(@RequestBody Object req, String command, Integer appId) {
//        logger.info("{}收到{}回调数据：{}",
//                appId, command, JSONObject.toJSONString(req));
//        logger.info(command);
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(req));
        String prompt = jsonObject.getString("messageBody");
        String imei = jsonObject.getString("imei");
        String toId = jsonObject.getString("toId");
        String fromId = jsonObject.getString("fromId");
        if ("message.send.after".equals(command) && "ChatGPT".equals(toId)) {
            Proxy proxy = Proxys.socks5("127.0.0.1", 7890);
            ChatGPTStream chatGPTStream = ChatGPTStream.builder()
                    .apiKey("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")
                    .proxy(proxy)
                    .apiHost("https://api.openai.com/") //反向代理地址
                    .build()
                    .init();
            SseEmitter sseEmitter = new SseEmitter(-1L);

            SseStreamListener listener = new SseStreamListener(sseEmitter);
            Message message = Message.of(prompt);

            //回答完成，可以做一些事情
            listener.setOnComplate(msg -> {
                SignApi signApi = new SignApi(appConfig.getAppId(),
                        appConfig.getPrivateKey());
                String sign = signApi.genUserSign(toId,
                        500000);
                String url = appConfig.getImUrl() + "/message/send";

                SendMessageProto proto = new SendMessageProto();
                proto.setImei(imei);
                proto.setFromId(toId);
                proto.setToId(fromId);
                proto.setClientType(1);
                proto.setMessageBody(JSONObject.toJSONString(new MessageBody(1, msg)));

                ConcurrentHashMap<String, Object> parameter = new ConcurrentHashMap<>();
                parameter.put("appId", appId);
                parameter.put("userSign", sign);
                parameter.put("identifier", toId);

                try {
                    ResponseVO responseVO = httpRequestUtils.doPost(url, ResponseVO.class, parameter, null, JSONObject.toJSONString(proto), "");
//                    return responseVO;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            chatGPTStream.streamChatCompletion(Collections.singletonList(message), listener);

        }


        return ResponseVO.successResponse(req);
    }

}
