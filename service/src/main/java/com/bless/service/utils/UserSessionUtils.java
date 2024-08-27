package com.bless.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.bless.common.constant.Constants;
import com.bless.common.enums.ImConnectStatusEnum;
import com.bless.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-27 12:30
 */
@Component
public class UserSessionUtils {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //1.获取用户所有的session
    public List<UserSession> getUserSession(Integer appId, String userId) {
        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);
        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o : values) {
            String str = (String) o;
            UserSession session = JSONObject.parseObject(str, UserSession.class);
            if (session.getConnectState() == ImConnectStatusEnum.ONLINE_STATUS.getCode()) {
                list.add(session);
            }
        }
        return list;
    }

    //获取指定端的session
    public UserSession getUserSession(Integer appId,String userId
            ,Integer clientType,String imei){

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants
                + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        UserSession session =
                JSONObject.parseObject(o.toString(), UserSession.class);
        return session;
    }
}
