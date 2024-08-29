package com.bless.service.group.service;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-28 21:29
 */




import com.bless.codec.pack.message.ChatMessageAck;
import com.bless.common.ResponseVO;
import com.bless.common.enums.command.GroupEventCommand;
import com.bless.common.enums.command.MessageCommand;
import com.bless.common.model.ClientInfo;
import com.bless.common.model.message.GroupChatMessageContent;
import com.bless.common.model.message.MessageContent;
import com.bless.service.group.model.req.SendGroupMessageReq;
import com.bless.service.message.model.resp.SendMessageResp;
import com.bless.service.message.service.CheckSendMessageService;

import com.bless.service.message.service.MessageStoreService;
import com.bless.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;
    @Autowired
    MessageStoreService messageStoreService;

    public void process(GroupChatMessageContent messageContent){

        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();
        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友
        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId,
                appId);
        if(responseVO.isOk()){
            messageStoreService.storeGroupMessage(messageContent);
            //1.回ack成功给自己
            ack(messageContent, responseVO);
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            dispatchMessage(messageContent);
        }else{
            //告诉客户端失败了
            //ack
            ack(messageContent, responseVO);
        }
    }

    //分发消息给发送端
    private void dispatchMessage(GroupChatMessageContent messageContent){

        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(),
                messageContent.getAppId());
        for (String memberId : groupMemberId) {
            if(!memberId.equals(messageContent.getFromId())){
                messageProducer.sendToUser(memberId,
                        GroupEventCommand.MSG_GROUP,
                        messageContent, messageContent.getAppId());
            }
        }
    }

    //返回ack信息
    private void ack(MessageContent messageContent, ResponseVO responseVO){

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(),
                GroupEventCommand.MSG_GROUP,
                responseVO, messageContent
        );
    }

    //发送消息给其他端
    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P,messageContent,messageContent);
    }


    //群的前置校验
    private ResponseVO imServerPermissionCheck(String fromId, String toId, Integer appId){
        ResponseVO responseVO = checkSendMessageService
                .checkGroupMessage(fromId, toId, appId);
        return responseVO;
    }

    public SendMessageResp send(SendGroupMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        GroupChatMessageContent message = new GroupChatMessageContent();
        BeanUtils.copyProperties(req,message);

        messageStoreService.storeGroupMessage(message);

        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        //2.发消息给同步在线端
        syncToSender(message,message);
        //3.发消息给对方在线端
        dispatchMessage(message);

        return sendMessageResp;

    }
}
