package com.catzhang.im.tcp.server;

import com.catzhang.im.codec.MessageDecoder;
import com.catzhang.im.codec.MessageEncoder;
import com.catzhang.im.codec.config.BootstrapConfig;
import com.catzhang.im.tcp.handler.HeartBeatHandler;
import com.catzhang.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author crazycatzhang
 */
public class TcpServer {

    private final static Logger logger = LoggerFactory.getLogger(TcpServer.class);

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ServerBootstrap server;
    BootstrapConfig.TcpConfig tcpConfig;

    public TcpServer(BootstrapConfig.TcpConfig tcpConfig) {
        this.tcpConfig = tcpConfig;
        bossGroup = new NioEventLoopGroup(tcpConfig.getBossThreadSize());
        workerGroup = new NioEventLoopGroup(tcpConfig.getWorkThreadSize());
        server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) // 服务端可连接队列大小
                .option(ChannelOption.SO_REUSEADDR, true) // 参数表示允许重复使用本地地址和端口
                .childOption(ChannelOption.TCP_NODELAY, true) // 是否禁用Nagle算法 简单点说是否批量发送数据 true关闭 false开启。 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 保活开关2h没有数据服务端会发送心跳包
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MessageDecoder());
                        socketChannel.pipeline().addLast(new MessageEncoder());
                        socketChannel.pipeline().addLast(new IdleStateHandler(0, 0, 10));
                        socketChannel.pipeline().addLast(new HeartBeatHandler(tcpConfig.getHeartbeatTimeout()));
                        socketChannel.pipeline().addLast(new NettyServerHandler(tcpConfig.getBrokerId(), tcpConfig.getLogicUrl()));
                    }
                });
    }

    public void start() {
        this.server.bind(this.tcpConfig.getTcpPort());
    }
}
