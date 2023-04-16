package com.catzhang.im.tcp;

import com.catzhang.im.codec.config.BootstrapConfig;
import com.catzhang.im.tcp.redis.RedisManager;
import com.catzhang.im.tcp.server.TcpServer;
import com.catzhang.im.tcp.server.WebSocketServer;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;

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

            new TcpServer(bootstrapConfig.getLim()).start();
            new WebSocketServer(bootstrapConfig.getLim()).start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }
    }

}
