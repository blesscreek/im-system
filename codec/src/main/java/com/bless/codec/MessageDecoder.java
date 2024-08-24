package com.bless.codec;

import com.alibaba.fastjson.JSON;
import com.bless.codec.proto.Message;
import com.bless.codec.proto.MessageHeader;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Author bless
 * @Version 1.0
 * @Description 消息解码器
 * @Date 2024-08-23 15:34
 */

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //请求头（指令
        // 版本
        // clientType
        // 消息解析类型
        // appId
        // imei长度
        // bodylen）+ imei号 + 请求体
        if (in.readableBytes() < 28) {
            return;
        }
        /**
         * 获取command
         */
        int command = in.readInt();

        /**
         * 获取version
         */
        int version = in.readInt();

        /**
         * 获取clientType
         */
        int clientType = in.readInt();

        /**
         * 获取messageType
         */
        int messageType = in.readInt();

        /**
         * 获取appId
         */
        int appId = in.readInt();

        /**
         * 获取imeiLength
         */
        int imeiLength = in.readInt();

        /**
         * 获取bodyLength
         */
        int bodyLength = in.readInt();
        if (in.readableBytes() < imeiLength + bodyLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        byte[] bodyData = new byte[bodyLength];
        in.readBytes(bodyData);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setClientType(clientType);
        messageHeader.setCommand(command);
        messageHeader.setLength(bodyLength);
        messageHeader.setVersion(version);
        messageHeader.setMessageType(messageType);
        messageHeader.setImei(imei);

        Message message = new Message();
        message.setMessageHeader(messageHeader);
        if (messageType == 0x0) {
            String body = new String(bodyData);
            JSONObject parse = (JSONObject)JSONObject.parse(body);
            message.setMessagePack(parse);
        }
        in.markReaderIndex();
        out.add(message);

    }
}
