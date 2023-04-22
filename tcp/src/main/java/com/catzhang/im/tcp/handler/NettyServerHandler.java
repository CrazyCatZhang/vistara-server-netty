package com.catzhang.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.catzhang.im.codec.pack.LoginPack;
import com.catzhang.im.codec.proto.Message;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.ImConnectStatus;
import com.catzhang.im.common.enums.command.SystemCommand;
import com.catzhang.im.common.model.UserClientDto;
import com.catzhang.im.common.model.UserSession;
import com.catzhang.im.tcp.redis.RedisManager;
import com.catzhang.im.tcp.utils.SessionSocketHolder;
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

        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            SessionSocketHolder.removeUserSession((NioSocketChannel) channelHandlerContext.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.READTIME)).set(System.currentTimeMillis());
        }

    }
}
