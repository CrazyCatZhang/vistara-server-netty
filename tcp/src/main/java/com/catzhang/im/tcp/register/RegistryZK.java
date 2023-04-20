package com.catzhang.im.tcp.register;

import com.catzhang.im.codec.config.BootstrapConfig;
import com.catzhang.im.common.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class RegistryZK implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RegistryZK.class);

    private ZKit zkit;

    private String ip;

    private BootstrapConfig.TcpConfig config;

    @Override
    public void run() {
        zkit.createRootNode();
        String tcpPath = Constants.IMCOREZKROOT + Constants.IMCOREZKROOTTCP + "/" + ip + ":" + config.getTcpPort();
        String webSocketPath = Constants.IMCOREZKROOT + Constants.IMCOREZKROOTWEBSOCKET + "/" + ip + ":" + config.getWebSocketPort();
        zkit.createNode(tcpPath);
        logger.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);
        zkit.createNode(webSocketPath);
        logger.info("Registry zookeeper webSocketPath success, msg=[{}]", webSocketPath);
    }
}
