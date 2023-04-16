package com.catzhang.im.codec;

import com.catzhang.im.codec.proto.Message;
import com.catzhang.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author crazycatzhang
 */
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 28) {
            return;
        }

        Message message = ByteBufToMessageUtils.transition(byteBuf);
        if (message == null) {
            return;
        }

        list.add(message);
    }
}
