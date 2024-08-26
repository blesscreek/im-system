package com.bless.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import com.bless.common.constant.Constants;
import com.bless.common.enums.ImConnectStatusEnum;
import com.bless.common.model.UserClientDto;
import com.bless.common.model.UserSession;
import com.bless.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-23 21:43
 */

public class SessionSocketHolder {
    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();
    public static void put(Integer appId,String userId,Integer clientType,
            NioSocketChannel channel, String imei){
        UserClientDto dto = new UserClientDto();
        dto.setImei(imei);
        dto.setAppId(appId);
        dto.setClientType(clientType);
        dto.setUserId(userId);
        CHANNELS.put(dto,channel);
    }
    public static NioSocketChannel get(Integer appId, String userId,
                                       Integer clientType, String imei) {
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setClientType(clientType);
        dto.setUserId(userId);
        dto.setImei(imei);
        return CHANNELS.get(dto);
    }
    public static List<NioSocketChannel> get(Integer appId , String id) {

        Set<UserClientDto> channelInfos = CHANNELS.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(channel ->{
            if(channel.getAppId().equals(appId) && id.equals(channel.getUserId())){
                channels.add(CHANNELS.get(channel));
            }
        });

        return channels;
    }

    public static void remove(Integer appId, String userId,
                              Integer clientType, String imei) {
        UserClientDto dto = new UserClientDto();
        dto.setImei(imei);
        dto.setAppId(appId);
        dto.setClientType(clientType);
        dto.setUserId(userId);
        CHANNELS.remove(dto);
    }
    public static void remove(NioSocketChannel channel) {
        CHANNELS.entrySet().stream().filter(entry->entry.getValue() == channel)
                .forEach(entry->CHANNELS.remove(entry.getKey()));

    }
    public static void removeUserSession(NioSocketChannel nioSocketChannel) {
        //删除session
        String userId =(String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId =(Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType =(Integer ) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

        SessionSocketHolder.remove(appId,userId,clientType,imei);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId +
                Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType + ":" + imei);
        nioSocketChannel.close();
    }

    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {
        String userId =(String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId =(Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType =(Integer ) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        SessionSocketHolder.remove(appId,userId,clientType,imei);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        String sessionStr= map.get(clientType.toString());
        if (!StringUtils.isBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType.toString() + ":" + imei, JSONObject.toJSONString(userSession));
        }
        nioSocketChannel.close();
    }
}
