package com.catzhang.im.codec.config;

import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class BootstrapConfig {

    private TcpConfig lim;

    @Data
    public static class TcpConfig {

        private Integer tcpPort;// tcp 绑定的端口号

        private Integer webSocketPort; // webSocket 绑定的端口号

        private Integer bossThreadSize; // boss线程 默认=1

        private Integer workThreadSize; //work线程

    }

}
