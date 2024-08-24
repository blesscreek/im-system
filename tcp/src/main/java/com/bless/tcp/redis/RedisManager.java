package com.bless.tcp.redis;

import com.bless.codec.config.BootstrapConfig;
import org.redisson.api.RedissonClient;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-24 12:06
 */

public class RedisManager {
    private static RedissonClient redissonClient;
    public static void init(BootstrapConfig config) {
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getIm().getRedis());
    }
    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }


}
