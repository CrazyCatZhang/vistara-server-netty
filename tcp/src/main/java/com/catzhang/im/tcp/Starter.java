package com.catzhang.im.tcp;

import com.catzhang.im.codec.config.BootstrapConfig;
import com.catzhang.im.tcp.consume.MessageConsumer;
import com.catzhang.im.tcp.redis.RedisManager;
import com.catzhang.im.tcp.register.RegistryZK;
import com.catzhang.im.tcp.register.ZKit;
import com.catzhang.im.tcp.server.TcpServer;
import com.catzhang.im.tcp.server.WebSocketServer;
import com.catzhang.im.tcp.utils.MqFactory;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author crazycatzhang
 */
public class Starter {

    public static void main(String[] args) {
        if (args.length > 0) {
            start(args[0]);
        }
    }


    private static void start(String path) {
        try {
            Yaml yaml = new Yaml();
            FileInputStream fileInputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(fileInputStream, BootstrapConfig.class);

            RedisManager.init(bootstrapConfig.getLim().getRedis());
            MqFactory.init(bootstrapConfig.getLim().getRabbitmq());
            MessageConsumer.init();
            registryZK(bootstrapConfig);

            new TcpServer(bootstrapConfig.getLim()).start();
            new WebSocketServer(bootstrapConfig.getLim()).start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }
    }

    public static void registryZK(BootstrapConfig config) throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getLim().getZkConfig().getZkAddr(), config.getLim().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        RegistryZK registryZK = new RegistryZK(zKit, ip, config.getLim());
        Thread thread = new Thread(registryZK);
        thread.start();
    }

}
