package com.catzhang.im.service.config;

import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.route.RouteHandle;
import com.catzhang.im.common.route.algorithm.consistenthash.ConsistentHashHandle;
import com.catzhang.im.common.route.algorithm.consistenthash.TreeMapConsistentHash;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author crazycatzhang
 */
@Configuration
public class BeanConfig {

    @Autowired
    AppConfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(),
                appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandle routeHandle() {
        return new ConsistentHashHandle(new TreeMapConsistentHash());
    }

}
