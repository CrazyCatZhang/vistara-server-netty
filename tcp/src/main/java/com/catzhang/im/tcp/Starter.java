package com.catzhang.im.tcp;

import com.catzhang.im.tcp.server.TcpServer;

/**
 * @author crazycatzhang
 */
public class Starter {

    public static void main(String[] args) {
        new TcpServer(9000);
    }

}
