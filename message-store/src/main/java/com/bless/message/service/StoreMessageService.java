package com.bless.message.service;

import com.bless.common.model.message.MessageContent;
import com.bless.message.dao.ImMessageBodyEntity;
import com.bless.message.dao.ImMessageHistoryEntity;
import com.bless.message.dao.mapper.ImMessageBodyMapper;
import com.bless.message.dao.mapper.ImMessageHistoryMapper;
import com.bless.message.model.DoStoreP2PMessageDto;
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
 * @Date 2024-08-31 10:03
 */
@Service
public class StoreMessageService {
    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;


    @Transactional
    public void doStoreP2PMessage(DoStoreP2PMessageDto doStoreP2PMessageDto){
        imMessageBodyMapper.insert(doStoreP2PMessageDto.getImMessageBodyEntity());
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(doStoreP2PMessageDto.getMessageContent(), doStoreP2PMessageDto.getImMessageBodyEntity());
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
    }


    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity){
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());
        fromHistory.setSequence(messageContent.getMessageSequence());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());
        toHistory.setSequence(messageContent.getMessageSequence());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }


}
