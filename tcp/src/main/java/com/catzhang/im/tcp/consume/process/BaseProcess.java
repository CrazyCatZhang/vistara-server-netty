package com.catzhang.im.tcp.consume.process;

import com.catzhang.im.codec.proto.MessagePack;
import com.catzhang.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author crazycatzhang
 */
public abstract class BaseProcess {

    protected abstract void processBefore();

    public void process(MessagePack messagePack) {

        processBefore();
        NioSocketChannel nioSocketChannel = SessionSocketHolder.get(messagePack.getAppId(), messagePack.getToId(), messagePack.getClientType(), messagePack.getImei());
        if (nioSocketChannel != null) {
            nioSocketChannel.writeAndFlush(messagePack);
        }
        processAfter();

    }

    protected abstract void processAfter();

}
