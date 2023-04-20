package com.catzhang.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.codec.proto.MessagePack;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author crazycatzhang
 * 消息编码类，私有协议规则，前4位表示command，接着是数据长度，后面是数据
 */
public class MessageEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object message, ByteBuf byteBuf) throws Exception {
        if (message instanceof MessagePack) {
            MessagePack msgBody = (MessagePack) message;
            String s = JSONObject.toJSONString(msgBody.getData());
            byte[] bytes = s.getBytes();
            byteBuf.writeInt(msgBody.getCommand());
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
