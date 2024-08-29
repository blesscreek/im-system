package com.bless.service.message.service;

import com.bless.common.enums.DelFlagEnum;
import com.bless.common.model.message.GroupChatMessageContent;
import com.bless.common.model.message.MessageContent;
import com.bless.service.group.dao.ImGroupMessageHistoryEntity;
import com.bless.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.bless.service.message.dao.ImMessageBodyEntity;
import com.bless.service.message.dao.ImMessageHistoryEntity;
import com.bless.service.message.dao.mapper.ImMessageBodyMapper;
import com.bless.service.message.dao.mapper.ImMessageHistoryMapper;
import com.bless.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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


    @Transactional
    public void storeP2PMessage(MessageContent messageContent){
        //messageContent 转化成 messageBody
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        //插入messageBody
        imMessageBodyMapper.insert(imMessageBodyEntity);
        //转化成MessageHistory
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
        //批量插入
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    public ImMessageBodyEntity extractMessageBody(MessageContent messageContent){
        ImMessageBodyEntity messageBody = new ImMessageBodyEntity();
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

        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        //插入messageBody
        imMessageBodyMapper.insert(imMessageBodyEntity);

        //转换成MessageHistory
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
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


}