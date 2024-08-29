package com.bless.service.config;

import com.bless.common.config.AppConfig;
import com.bless.common.enums.ImUrlRouteWayEnum;
import com.bless.common.enums.RouteHashMethodEnum;
import com.bless.common.route.RouteHandle;
import com.bless.common.route.algorithm.consistanthash.AbstractConsistentHash;
import com.bless.common.route.algorithm.consistanthash.ConsistentHashHandel;
import com.bless.common.route.algorithm.consistanthash.TreeMapConsistentHash;
import com.bless.common.route.algorithm.loop.LoopHandle;
import com.bless.common.route.algorithm.random.RandomHandle;
import com.bless.service.utils.SnowflakeIdWorker;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-26 12:50
 */
@Configuration
public class BeanConfig {
    @Autowired
    AppConfig appConfig;
    @Bean
    public ZkClient buildZkClient() {
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }
    @Bean
    public RouteHandle routeHandle() throws Exception {
        Integer imRouteWay = appConfig.getImRouteWay();
        String routeWay = "";
        ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
        routeWay = handler.getClazz();
        RouteHandle routeHandle = (RouteHandle) Class.forName(routeWay).newInstance();
        if (handler == ImUrlRouteWayEnum.HASH) {
            Method setHash = Class.forName(routeWay).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashWay = "";
            RouteHashMethodEnum hashHandler = RouteHashMethodEnum.getHandler(consistentHashWay);
            hashWay = hashHandler.getClazz();
            AbstractConsistentHash consistentHash = (AbstractConsistentHash) Class.forName(hashWay).newInstance();
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
