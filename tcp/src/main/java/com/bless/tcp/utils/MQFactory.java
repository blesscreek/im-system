package com.bless.tcp.utils;

import com.bless.codec.config.BootstrapConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-24 20:18
 */

public class MQFactory {
    private static ConnectionFactory factory = null;

    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    private static Connection getConnection() throws IOException, TimeoutException {
        Connection connection = factory.newConnection();
        return connection;
    }
    public static Channel getChanel(String channelName) throws IOException, TimeoutException {
        //Channel 是用于发布、订阅消息的抽象层，它与 Connection 共享，用来执行具体的 AMQP 操作。
        Channel channel = channelMap.get(channelName);
        if (channel == null) {
            channel = getConnection().createChannel();
            channelMap.put(channelName, channel);
        }
        return channel;
    }

    public static void init(BootstrapConfig.Rabbitmq rabbitmq) {
        if (factory == null) {
            factory = new ConnectionFactory();
            factory.setHost(rabbitmq.getHost());
            factory.setPort(rabbitmq.getPort());
            factory.setUsername(rabbitmq.getUserName());
            factory.setPassword(rabbitmq.getPassword());
            factory.setVirtualHost(rabbitmq.getVirtualHost());
        }
    }
}
