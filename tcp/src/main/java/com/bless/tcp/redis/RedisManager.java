package com.bless.tcp.redis;

import com.bless.codec.config.BootstrapConfig;
import com.bless.tcp.reciver.UserLoginMessageListener;
import org.redisson.api.RedissonClient;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-24 12:06
 */

public class RedisManager {
    private static RedissonClient redissonClient;

    private static Integer loginModel;
    public static void init(BootstrapConfig config) {
        loginModel = config.getIm().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getIm().getRedis());
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();

    }
    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }


}
