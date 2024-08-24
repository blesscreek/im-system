package com.bless.tcp.publish;

import com.alibaba.fastjson.JSONObject;
import com.bless.tcp.utils.MQFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-24 20:32
 */
@Slf4j
public class MqMessageProducer {
    public static void sendMessage(Object message) {
        Channel channel = null;
        String channelName = "";
        try {
            channel = MQFactory.getChanel(channelName);
            channel.basicPublish(channelName,"",
                    null, JSONObject.toJSONString(message).getBytes());
        }catch (Exception e) {
            log.error("发送消息出现异常：{}",e.getMessage());
        }
    }
}
