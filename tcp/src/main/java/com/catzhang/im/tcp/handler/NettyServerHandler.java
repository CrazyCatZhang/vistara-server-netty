package com.catzhang.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.catzhang.im.codec.pack.LoginPack;
import com.catzhang.im.codec.proto.Message;
import com.catzhang.im.common.enums.command.SystemCommand;
import com.catzhang.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author crazycatzhang
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {

        Integer command = message.getMessageHeader().getCommand();
        if (command == SystemCommand.LOGIN.getCommand()) {
            LoginPack loginPack = JSON.parseObject(JSON.toJSONString(message.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());
            logger.info(loginPack.toString());
            channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(loginPack.getUserId());
            SessionSocketHolder.put(loginPack.getUserId(), (NioSocketChannel) channelHandlerContext.channel());
        }

    }
}
