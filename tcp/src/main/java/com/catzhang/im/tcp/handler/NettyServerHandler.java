package com.catzhang.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.catzhang.im.codec.pack.LoginPack;
import com.catzhang.im.codec.pack.message.ChatMessageAck;
import com.catzhang.im.codec.pack.user.LoginAckPack;
import com.catzhang.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.catzhang.im.codec.proto.Message;
import com.catzhang.im.codec.proto.MessagePack;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ConnectStatus;
import com.catzhang.im.common.enums.ImConnectStatus;
import com.catzhang.im.common.enums.command.*;
import com.catzhang.im.common.model.UserClientDto;
import com.catzhang.im.common.model.UserSession;
import com.catzhang.im.common.model.message.VerifySendMessageReq;
import com.catzhang.im.tcp.feign.FeignMessageService;
import com.catzhang.im.tcp.publish.MessageProducer;
import com.catzhang.im.tcp.redis.RedisManager;
import com.catzhang.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * @author crazycatzhang
 */
@AllArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer brokerId;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.brokerId = brokerId;
        this.feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))//设置超时时间
                .target(FeignMessageService.class, logicUrl);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {

        Integer command = message.getMessageHeader().getCommand();
        if (command == SystemCommand.LOGIN.getCommand()) {
            LoginPack loginPack = JSON.parseObject(JSON.toJSONString(message.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());


            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.USERID)).set(loginPack.getUserId());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.APPID)).set(message.getMessageHeader().getAppId());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.CLIENTTYPE)).set(message.getMessageHeader().getClientType());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.IMEI)).set(message.getMessageHeader().getImei());


            UserSession userSession = new UserSession();
            userSession.setAppId(message.getMessageHeader().getAppId());
            userSession.setClientType(message.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatus.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(message.getMessageHeader().getImei());
            try {
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                userSession.setBrokerHost(hostAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }

            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(message.getMessageHeader().getAppId() + Constants.RedisConstants.USER_SESSION_CONSTANTS + loginPack.getUserId());
            map.put(message.getMessageHeader().getClientType() + ":" + message.getMessageHeader().getImei(), JSON.toJSONString(userSession));

            SessionSocketHolder.put(message.getMessageHeader().getAppId(), loginPack.getUserId(), message.getMessageHeader().getClientType(), message.getMessageHeader().getImei(), (NioSocketChannel) channelHandlerContext.channel());

            UserClientDto userClientDto = new UserClientDto();
            userClientDto.setUserId(loginPack.getUserId());
            userClientDto.setClientType(message.getMessageHeader().getClientType());
            userClientDto.setImei(message.getMessageHeader().getImei());
            userClientDto.setAppId(message.getMessageHeader().getAppId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.USERLOGINCHANNEL);
            topic.publish(JSONObject.toJSONString(userClientDto));

            UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
            userStatusChangeNotifyPack.setAppId(message.getMessageHeader().getAppId());
            userStatusChangeNotifyPack.setUserId(loginPack.getUserId());
            userStatusChangeNotifyPack.setStatus(ConnectStatus.ONLINE_STATUS.getCode());
            MessageProducer.sendMessage(userStatusChangeNotifyPack, message.getMessageHeader(), UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

            MessagePack<LoginAckPack> loginSuccess = new MessagePack<>();
            LoginAckPack loginAckPack = new LoginAckPack();
            loginAckPack.setUserId(loginPack.getUserId());
            loginSuccess.setCommand(SystemCommand.LOGINACK.getCommand());
            loginSuccess.setData(loginAckPack);
            loginSuccess.setImei(message.getMessageHeader().getImei());
            loginSuccess.setAppId(message.getMessageHeader().getAppId());
            channelHandlerContext.channel().writeAndFlush(loginSuccess);


        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            logger.info("用户已登出.........");
            SessionSocketHolder.removeUserSession((NioSocketChannel) channelHandlerContext.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.READTIME)).set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand() || command == GroupEventCommand.MSG_GROUP.getCommand() || command == MediaEventCommand.CALL_VIDEO.getCommand() || command == MediaEventCommand.CALL_VOICE.getCommand()) {
            try {
                String toId;
                VerifySendMessageReq verifySendMessageReq = new VerifySendMessageReq();
                verifySendMessageReq.setCommand(message.getMessageHeader().getCommand());
                verifySendMessageReq.setAppId(message.getMessageHeader().getAppId());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()));
                verifySendMessageReq.setFromId(jsonObject.getString("fromId"));
                if (command == MessageCommand.MSG_P2P.getCommand() || command == MediaEventCommand.CALL_VIDEO.getCommand() || command == MediaEventCommand.CALL_VOICE.getCommand()) {
                    toId = jsonObject.getString("toId");
                } else {
                    toId = jsonObject.getString("groupId");
                }
                verifySendMessageReq.setToId(toId);
                ResponseVO responseVO = feignMessageService.verifySendMessage(verifySendMessageReq);
                if (responseVO.isOk()) {
                    MessageProducer.sendMessage(message, command);
                } else {

                    Integer ackCommand;
                    if (command == MessageCommand.MSG_P2P.getCommand()) {
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    } else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }

                    ChatMessageAck messageId = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(messageId);
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setData(responseVO);
                    ack.setCommand(ackCommand);
                    channelHandlerContext.channel().writeAndFlush(ack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MessageProducer.sendMessage(message, command);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }
}
