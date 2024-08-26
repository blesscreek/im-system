package com.bless.tcp.reciver;

import com.bless.common.constant.Constants;
import com.bless.tcp.utils.MQFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;

import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-24 20:36
 */
@Slf4j
public class MessageReciver {
    private static String brokerId;
    public static void startReciverMessage() {
        try {
            Channel chanel = MQFactory.
                    getChanel(Constants.RabbitConstants.MessageService2Im + brokerId);
//            //使用 exchangeDeclare 声明一个名为 MessageService2Im 的交换机，类型为 direct，
//            // 表示消息将被路由到绑定到该交换机的特定队列。true 参数表示交换机是持久化的。
//            chanel.exchangeDeclare(Constants.RabbitConstants.MessageService2Im + brokerId,
//                    "direct", true);
            //使用 queueDeclare 声明一个名为 MessageService2Im 的队列。
            // 此队列是持久化的（true），即使 RabbitMQ 服务器重启，队列也会保留。
            chanel.queueDeclare(Constants.RabbitConstants.MessageService2Im  + brokerId,
                    true, false,false ,null );
            //将队列 MessageService2Im 绑定到同名的 MessageService2Im 交换机。
            // 绑定的路由键为brokerId，表示所有发往该交换机的消息都会路由到该队列。
            chanel.queueBind(Constants.RabbitConstants.MessageService2Im + brokerId,
                    Constants.RabbitConstants.MessageService2Im,brokerId);
            //开始消费来自队列的消息。第二个参数 false 表示手动消息确认（在消息处理完毕后，客户端需要手动发送 ack）。
            chanel.basicConsume(Constants.RabbitConstants.MessageService2Im + brokerId,
                    false,
                    //处理消息的类，重写了 handleDelivery 方法。
                    // 每次收到消息时，handleDelivery 会被调用，body 是消息的内容。
                    new DefaultConsumer(chanel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            //TODO 处理消息服务发来的消息
                            String msgStr = new String(body);
                            log.info(msgStr);
                        }
                    });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        startReciverMessage();
    }

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReciver.brokerId)) {
            MessageReciver.brokerId = brokerId;
        }
        startReciverMessage();
    }
}

