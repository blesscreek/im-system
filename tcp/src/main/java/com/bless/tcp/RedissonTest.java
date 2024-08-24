package com.bless.tcp;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

public class RedissonTest {

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setDatabase(1);
        StringCodec stringCodec = new StringCodec();
        config.setCodec(stringCodec);
        RedissonClient redissonClient = Redisson.create(config);

//        RBucket<Object> im = redissonClient.getBucket("im");
//        System.out.println(im.get());
//        im.set("im");
//        System.out.println(im.get());

        RMap<String, String> imMap = redissonClient.getMap("imMap");
        String client = imMap.get("client");
        System.out.println(client);
        imMap.put("client","webClient");
        System.out.println(imMap.get("client"));

//        RTopic topic1 = redissonClient.getTopic("topic");
//        topic1.addListener(String.class, new MessageListener<String>() {
//            @Override
//            public void onMessage(CharSequence charSequence, String s) {
//                System.out.println("client1收到消息：" + s);
//            }
//        });
//
//        RTopic topic2 = redissonClient.getTopic("topic");
//        topic2.addListener(String.class, new MessageListener<String>() {
//            @Override
//            public void onMessage(CharSequence charSequence, String s) {
//                System.out.println("client2收到消息：" + s);
//            }
//        });
//
//        RTopic topic3 = redissonClient.getTopic("topic");
//        topic3.publish("hello");

    }

}