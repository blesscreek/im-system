package com.bless.tcp;

import com.bless.codec.config.BootstrapConfig;
import com.bless.tcp.publish.MqMessageProducer;
import com.bless.tcp.reciver.MessageReciver;
import com.bless.tcp.redis.RedisManager;
import com.bless.tcp.server.ImServer;

import com.bless.tcp.server.ImWebSocketServer;
import com.bless.tcp.utils.MQFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-23 11:31
 */

public class Starter {

    public static void main(String[] args) {
        if(args.length > 0){
            start(args[0]);
        }
    }

    private static void start(String path){
        try{
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new ImServer(bootstrapConfig.getIm()).start();
            new ImWebSocketServer(bootstrapConfig.getIm()).start();
            RedisManager.init(bootstrapConfig);
            MQFactory.init(bootstrapConfig.getIm().getRabbitmq());
            MessageReciver.init();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(500);
        }
    }
}