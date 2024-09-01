package com.bless.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.bless.common.config.AppConfig;
import com.bless.common.constant.Constants;
import com.bless.common.enums.ConversationTypeEnum;
import com.bless.common.enums.DelFlagEnum;
import com.bless.common.model.message.*;
import com.bless.service.conversation.service.ConversationService;
import com.bless.service.group.dao.ImGroupMessageHistoryEntity;
import com.bless.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.bless.service.message.dao.ImMessageBodyEntity;
import com.bless.service.message.dao.ImMessageHistoryEntity;
import com.bless.service.message.dao.mapper.ImMessageBodyMapper;
import com.bless.service.message.dao.mapper.ImMessageHistoryMapper;
import com.bless.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
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

    @Autowired
    AppConfig appConfig;
    @Autowired
    ConversationService conversationService;

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

    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {
        // 找到fromId的队列
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        // 找到toId的队列
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getToId();

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        if (operations.zCard(fromKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(fromKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getFromId(),offlineMessage.getToId()
        ));
        operations.add(fromKey, JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());

        //判断 队列中的数据是否超过设定值
        if(operations.zCard(toKey) > appConfig.getOfflineMessageCount()){
            operations.removeRange(toKey,0,0);
        }

        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getToId(),offlineMessage.getFromId()
        ));
        // 插入 数据 根据messageKey 作为分值
        operations.add(toKey,JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());
    }

    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage
            ,List<String> memberIds){
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());
        for (String memberId : memberIds) {
            // 找到toId的队列
            String toKey = offlineMessage.getAppId() + ":" +
                    Constants.RedisConstants.OfflineMessage + ":" +
                    memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(),memberId,offlineMessage.getToId()
            ));
            if(operations.zCard(toKey) > appConfig.getOfflineMessageCount()){
                operations.removeRange(toKey,0,0);
            }
            // 插入 数据 根据messageKey 作为分值
            operations.add(toKey,JSONObject.toJSONString(offlineMessage),
                    offlineMessage.getMessageKey());
        }
    }
}