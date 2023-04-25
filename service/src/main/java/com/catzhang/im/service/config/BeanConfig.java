package com.catzhang.im.service.config;

import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.enums.ImUrlRouteWay;
import com.catzhang.im.common.enums.RouteHashMethod;
import com.catzhang.im.common.route.RouteHandle;
import com.catzhang.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import com.catzhang.im.service.utils.SnowflakeIdWorker;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

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
    public RouteHandle routeHandle() throws Exception {

        Integer imRouteWay = appConfig.getImRouteWay();
        String routeWay;

        ImUrlRouteWay handler = ImUrlRouteWay.getHandler(imRouteWay);
        assert handler != null;
        routeWay = handler.getClazz();

        RouteHandle routeHandle = (RouteHandle) Class.forName(routeWay).newInstance();
        if (handler == ImUrlRouteWay.HASH) {

            Method setHash = Class.forName(routeWay).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashWay;

            RouteHashMethod hashHandler = RouteHashMethod.getHandler(consistentHashWay);
            assert hashHandler != null;
            hashWay = hashHandler.getClazz();
            AbstractConsistentHash consistentHash
                    = (AbstractConsistentHash) Class.forName(hashWay).newInstance();
            setHash.invoke(routeHandle, consistentHash);
        }

        return routeHandle;

    }

    @Bean
    public EasySqlInjector easySqlInjector() {
        return new EasySqlInjector();
    }

    @Bean
    public SnowflakeIdWorker buildSnowflakeSeq() throws Exception {
        return new SnowflakeIdWorker(0);
    }
}
