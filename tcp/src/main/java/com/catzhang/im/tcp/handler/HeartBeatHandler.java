package com.catzhang.im.tcp.handler;

import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.tcp.publish.MessageProducer;
import com.catzhang.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author crazycatzhang
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);


    private final Long heartbeatTimeOut;

    public HeartBeatHandler(Long heartbeatTimeOut) {
        this.heartbeatTimeOut = heartbeatTimeOut;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        logger.info("进入了心跳处理Handler！！！！！！！！");
        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;        // 强制类型转换
            if (event.state() == IdleState.READER_IDLE) {
                logger.info("读空闲");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                logger.info("进入写空闲");
            } else if (event.state() == IdleState.ALL_IDLE) {
                Long lastReadTime = (Long) ctx.channel().attr(AttributeKey.valueOf(Constants.READTIME)).get();
                long now = System.currentTimeMillis();
                if (lastReadTime != null && now - lastReadTime > heartbeatTimeOut) {
                    SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
                }
            }
        }
    }

}
