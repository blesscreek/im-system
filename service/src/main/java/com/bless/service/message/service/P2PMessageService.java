package com.bless.service.message.service;

import com.bless.codec.pack.message.ChatMessageAck;
import com.bless.common.ResponseVO;
import com.bless.common.enums.command.MessageCommand;
import com.bless.common.model.ClientInfo;
import com.bless.common.model.message.MessageContent;
import com.bless.service.message.model.req.SendMessageReq;
import com.bless.service.message.model.resp.SendMessageResp;
import com.bless.service.utils.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-28 17:26
 */
@Service
public class P2PMessageService {
    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);
    @Autowired
    CheckSendMessageService checkSendMessageService;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    MessageStoreService messageStoreService;

    //前置校验
    //这个用户是否被禁言 是否被禁用
    //发送方和接收方是否是好友
    public void process(MessageContent messageContent){
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友
        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, messageContent);
        if(responseVO.isOk()){
            messageStoreService.storeP2PMessage(messageContent);
            //1.回ack成功给自己
            ack(messageContent, responseVO);
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            dispatchMessage(messageContent);
        } else {
            //告诉客户端失败了
            //ack
            ack(messageContent, responseVO);
        }
    }
    private List<ClientInfo> dispatchMessage(MessageContent messageContent){
        List<ClientInfo> clientInfos = messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P,
                messageContent, messageContent.getAppId());
        return clientInfos;
    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        logger.info("msg ack,msgId={},checkResut{}",messageContent.getMessageId(),responseVO.getCode());
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(),
                MessageCommand.MSG_ACK, responseVO, messageContent);
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P,messageContent,messageContent);
    }
    public ResponseVO imServerPermissionCheck(String fromId, String toId,
                                              MessageContent messageContent) {
        ResponseVO responseVO = checkSendMessageService.checkSenderForvidAndMute(fromId, messageContent.getAppId());
        if(!responseVO.isOk()){
            return responseVO;
        }
        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, messageContent.getAppId());
        return responseVO;
    }

    public SendMessageResp send(SendMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req,message);
        //插入数据
        messageStoreService.storeP2PMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        //2.发消息给同步在线端
        syncToSender(message,message);
        //3.发消息给对方在线端
        dispatchMessage(message);
        return sendMessageResp;
    }
}
