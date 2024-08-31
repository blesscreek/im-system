package com.bless.service.message.service;

import com.bless.common.enums.command.MessageCommand;
import com.bless.common.model.message.MessageReciveAckContent;
import com.bless.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-31 12:46
 */
@Service
public class MessageSyncService {
    @Autowired
    MessageProducer messageProducer;

    public void receiveMark(MessageReciveAckContent messageReciveAckContent) {
        messageProducer.sendToUser(messageReciveAckContent.getToId(),
                MessageCommand.MSG_RECIVE_ACK, messageReciveAckContent, messageReciveAckContent.getAppId());
    }
}
