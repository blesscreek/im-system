package com.bless.tcp.reciver.process;

import com.bless.codec.proto.MessagePack;
import com.bless.tcp.utils.SessionSocketHolder;
import com.rabbitmq.client.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-27 17:54
 */

public abstract class BaseProcess {
    public abstract void processBefore();

    public void process(MessagePack messagePack) {
        processBefore();
        NioSocketChannel channel = SessionSocketHolder.get(messagePack.getAppId(),
                messagePack.getToId(), messagePack.getClientType(),
                messagePack.getImei());
        if (channel != null) {
            channel.writeAndFlush(messagePack);
        }
        processAfter();
    }
    public abstract void processAfter();
}
