package com.bless.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.bless.common.constant.Constants;
import com.bless.common.enums.DelFlagEnum;
import com.bless.common.model.message.*;
import com.bless.service.group.dao.ImGroupMessageHistoryEntity;
import com.bless.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.bless.service.message.dao.ImMessageBodyEntity;
import com.bless.service.message.dao.ImMessageHistoryEntity;
import com.bless.service.message.dao.mapper.ImMessageBodyMapper;
import com.bless.service.message.dao.mapper.ImMessageHistoryMapper;
import com.bless.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-29 21:11
 */
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent){
//        //messageContent 转化成 messageBody
//        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
//        //插入messageBody
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        //转化成MessageHistory
//        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
//        //批量插入
//        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());

        ImMessageBody imMessageBody= extractMessageBody(messageContent);
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(imMessageBody);
        messageContent.setMessageKey(imMessageBody.getMessageKey());
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage,"",
                JSONObject.toJSONString(dto));

    }

    public ImMessageBody extractMessageBody(MessageContent messageContent){
        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(snowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity){
        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent){

        ImMessageBody imMessageBody = extractMessageBody(messageContent);

        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setMessageBody(imMessageBody);
        dto.setGroupChatMessageContent(messageContent);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreGroupMessage,
                "",
                JSONObject.toJSONString(dto));
        messageContent.setMessageKey(imMessageBody.getMessageKey());

//        //插入messageBody
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//
//        //转换成MessageHistory
//        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
//        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent
                                                                             messageContent , ImMessageBodyEntity messageBodyEntity){
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

    public void setMessageFromMessageIdCache(Integer appId,String messageId,Object messageContent){
        //appid : cache : messageId
        String key =appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(messageContent),300, TimeUnit.SECONDS);
    }

    public <T> T getMessageFromMessageIdCache(Integer appId,
                                              String messageId,Class<T> clazz){
        //appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isBlank(msg)){
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }

}