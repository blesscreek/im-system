package com.bless.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bless.codec.pack.LoginPack;
import com.bless.codec.proto.Message;
import com.bless.common.constant.Constants;
import com.bless.common.enums.ImConnectStatusEnum;
import com.bless.common.enums.command.SystemCommand;
import com.bless.common.model.UserSession;
import com.bless.tcp.redis.RedisManager;
import com.bless.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-23 15:47
 */

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Integer command = msg.getMessageHeader().getCommand();
        //登录command
        if (command == SystemCommand.LOGIN.getCommand()) {
            //把JSON字符串解析为带有泛型类型的对象，通过TypeReference显示指定泛型类型
            LoginPack loginpack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()), new TypeReference<LoginPack>() {}.getType());
            //把用户id与当前连接的通道管理起来，以便后续可以通过通道获取用户信息
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginpack.getUserId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(msg.getMessageHeader().getClientType());

            //Redis map
            UserSession userSession = new UserSession();
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setUserId(loginpack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());

            //存到redis中
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginpack.getUserId());
            map.put(msg.getMessageHeader().getClientType() + "", JSONObject.toJSONString(userSession));

            //将channel存起来
            SessionSocketHolder.put(msg.getMessageHeader().getAppId(), loginpack.getUserId()
                                    ,msg.getMessageHeader().getClientType(), (NioSocketChannel) ctx.channel());
        }else if (command == SystemCommand.LOGOUT.getCommand()) {
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());

        } else if(command == SystemCommand.PING.getCommand()) {
            ctx.channel()
                    .attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }
}
