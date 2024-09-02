package com.bless.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bless.codec.pack.LoginPack;
import com.bless.codec.pack.message.ChatMessageAck;
import com.bless.codec.proto.Message;
import com.bless.codec.proto.MessagePack;
import com.bless.common.ResponseVO;
import com.bless.common.constant.Constants;
import com.bless.common.enums.ImConnectStatusEnum;
import com.bless.common.enums.command.GroupEventCommand;
import com.bless.common.enums.command.MessageCommand;
import com.bless.common.enums.command.SystemCommand;
import com.bless.common.model.UserClientDto;
import com.bless.common.model.UserSession;
import com.bless.common.model.message.CheckSendMessageReq;
import com.bless.tcp.feign.FeignMessageService;
import com.bless.tcp.publish.MqMessageProducer;
import com.bless.tcp.redis.RedisManager;
import com.bless.tcp.utils.SessionSocketHolder;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.ComplexTypeModel;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-23 15:47
 */

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer brokerId;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.brokerId = brokerId;
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))//设置超时时间
                .target(FeignMessageService.class, logicUrl);
    }

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
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(msg.getMessageHeader().getImei());

            //Redis map
            UserSession userSession = new UserSession();
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setUserId(loginpack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(msg.getMessageHeader().getImei());
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }


            //存到redis中
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginpack.getUserId());
            map.put(msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei()
                    , JSONObject.toJSONString(userSession));

            //将channel存起来
            SessionSocketHolder.put(msg.getMessageHeader().getAppId()
                    , loginpack.getUserId()
                    ,msg.getMessageHeader().getClientType()
                    , (NioSocketChannel) ctx.channel()
                    , msg.getMessageHeader().getImei());

            //广播发布用户上线通知，方便踢端口下线
            UserClientDto dto = new UserClientDto();
            dto.setImei(msg.getMessageHeader().getImei());
            dto.setUserId(loginpack.getUserId());
            dto.setClientType(msg.getMessageHeader().getClientType());
            dto.setAppId(msg.getMessageHeader().getAppId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(dto));



        }else if (command == SystemCommand.LOGOUT.getCommand()) {
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());

        } else if(command == SystemCommand.PING.getCommand()) {
            ctx.channel()
                    .attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand()
        || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            try {
                String toId = "";
                CheckSendMessageReq req = new CheckSendMessageReq();
                req.setAppId(msg.getMessageHeader().getAppId());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                if(command == MessageCommand.MSG_P2P.getCommand()){
                    toId = jsonObject.getString("toId");
                }else {
                    toId = jsonObject.getString("groupId");
                }
                req.setFromId(fromId);
                req.setToId(toId);

                ResponseVO responseVO = feignMessageService.checkSendMessage(req);
                if (responseVO.isOk()) {
                    MqMessageProducer.sendMessage(msg, command);
                } else {
                    Integer ackCommand = 0;
                    if(command == MessageCommand.MSG_P2P.getCommand()){
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    }else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }
                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(chatMessageAck);
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setData(responseVO);
                    ack.setCommand(ackCommand );
                    ctx.channel().writeAndFlush(ack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            MqMessageProducer.sendMessage(msg, command);
        }

    }
    //表示 channel 处于不活动状态
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("Channel inactive, setting user session to offline.");
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }
}



