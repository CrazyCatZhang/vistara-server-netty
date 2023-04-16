package com.catzhang.im.codec;

import com.catzhang.im.codec.proto.Message;
import com.catzhang.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * @author crazycatzhang
 */
public class WebSocketMessageDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, BinaryWebSocketFrame binaryWebSocketFrame, List<Object> list) throws Exception {

        ByteBuf content = binaryWebSocketFrame.content();
        if (content.readableBytes() < 28) {
            return;
        }
        Message message = ByteBufToMessageUtils.transition(content);
        if(message == null){
            return;
        }
        list.add(message);

    }
}
