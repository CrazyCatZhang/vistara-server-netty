package com.catzhang.im.tcp.register;

import com.catzhang.im.common.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.I0Itec.zkclient.ZkClient;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class ZKit {

    private ZkClient zkClient;

    public void createRootNode() {
        boolean exists = zkClient.exists(Constants.IMCOREZKROOT);
        if (!exists) {
            zkClient.createPersistent(Constants.IMCOREZKROOT);
        }
        boolean tcpExists = zkClient.exists(Constants.IMCOREZKROOT + Constants.IMCOREZKROOTTCP);
        if (!tcpExists) {
            zkClient.createPersistent(Constants.IMCOREZKROOT + Constants.IMCOREZKROOTTCP);
        }
        boolean webExists = zkClient.exists(Constants.IMCOREZKROOT + Constants.IMCOREZKROOTWEBSOCKET);
        if (!webExists) {
            zkClient.createPersistent(Constants.IMCOREZKROOT + Constants.IMCOREZKROOTWEBSOCKET);
        }
    }

    public void createNode(String path) {

        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path);
        }

    }

}
