package com.bless.tcp.reciver;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-25 20:54
 */

import com.alibaba.fastjson.JSONObject;
import com.bless.codec.proto.MessagePack;
import com.bless.common.ClientType;
import com.bless.common.constant.Constants;
import com.bless.common.enums.DeviceMultiLoginEnum;
import com.bless.common.enums.command.SystemCommand;
import com.bless.common.model.UserClientDto;
import com.bless.tcp.redis.RedisManager;
import com.bless.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 多端同步：1 单端登录：一端在线：踢掉除了本clinetType + imel 的设备
 *         2 双端登录：允许pc/mobile 其中一端登录 + web端 踢掉除了本clinetType + imel 以外的web端设备
 *         3三端登录：允许手机+pc+web，踢掉同端的其他imei 除了web
 *         4不做任何处理
 */
public class UserLoginMessageListener {
    private final static Logger logger = LoggerFactory.getLogger(UserLoginMessageListener.class);

    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    public void listenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        //指定的topic上添加监听器。当有新消息发布到该主题时，监听器会触发并执行处理逻辑
        //第一个参数，指定监听消息类型为String，第二个参数是一个回调，当消息到达时会触发回调逻辑
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String msg) {
                logger.info("收到用户上线通知：" + msg);
                UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());
                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {
                    if (loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()) {
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    } else if (loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()) {
                        if(dto.getClientType() == ClientType.WEB.getCode()){
                            //当前是web端，web允许多端登录，不处理
                            continue;
                        }
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();

                        if (clientType == ClientType.WEB.getCode()){
                            //循环到web端，则直接不做处理
                            continue;
                        }
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        if(!(clientType + ":" + imei).equals(dto.getClientType()+":"+dto.getImei())){
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }

                    }else if(loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()){

                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        if(dto.getClientType() == ClientType.WEB.getCode()){
                            continue;
                        }

                        //IOS、ANDROID只允许一个登录，MAC、WINDOWS只允许一个登录
                        Boolean isSameClient = false;
                        if((clientType == ClientType.IOS.getCode() ||
                                clientType == ClientType.ANDROID.getCode()) &&
                                (dto.getClientType() == ClientType.IOS.getCode() ||
                                        dto.getClientType() == ClientType.ANDROID.getCode())){
                            isSameClient = true;
                        }

                        if((clientType == ClientType.MAC.getCode() ||
                                clientType == ClientType.WINDOWS.getCode()) &&
                                (dto.getClientType() == ClientType.MAC.getCode() ||
                                        dto.getClientType() == ClientType.WINDOWS.getCode())){
                            isSameClient = true;
                        }

                        if(isSameClient && !(clientType + ":" + imei).equals(dto.getClientType()+":"+dto.getImei())){
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    }

                }
            }
        });

    }

}
