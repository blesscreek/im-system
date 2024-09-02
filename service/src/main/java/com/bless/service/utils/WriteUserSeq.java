package com.bless.service.utils;

import com.bless.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-09-01 12:12
 */
@Service
public class WriteUserSeq {
    @Autowired
    RedisTemplate redisTemplate;
    public void writeUserSeq(Integer appId,String userId,String type,Long seq){
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key,type,seq);
    }


}
