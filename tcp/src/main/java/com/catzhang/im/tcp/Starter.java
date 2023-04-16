package com.catzhang.im.tcp;

import com.catzhang.im.tcp.server.TcpServer;
import com.catzhang.im.tcp.server.WebSocketServer;

/**
 * @author crazycatzhang
 */
public class Starter {

    public static void main(String[] args) {
        new TcpServer(9000);
        new WebSocketServer(19000);
    }

}
