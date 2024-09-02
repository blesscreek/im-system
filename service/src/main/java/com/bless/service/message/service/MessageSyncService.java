package com.bless.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.bless.codec.pack.message.MessageReadedPack;
import com.bless.codec.proto.Message;
import com.bless.common.ResponseVO;
import com.bless.common.constant.Constants;
import com.bless.common.enums.command.Command;
import com.bless.common.enums.command.GroupEventCommand;
import com.bless.common.enums.command.MessageCommand;
import com.bless.common.model.SyncReq;
import com.bless.common.model.SyncResp;
import com.bless.common.model.message.MessageReadedContent;
import com.bless.common.model.message.MessageReciveAckContent;
import com.bless.common.model.message.OfflineMessageContent;
import com.bless.service.conversation.service.ConversationService;
import com.bless.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    @Autowired
    ConversationService conversationService;
    @Autowired
    RedisTemplate redisTemplate;

    public void receiveMark(MessageReciveAckContent messageReciveAckContent) {
        messageProducer.sendToUser(messageReciveAckContent.getToId(),
                MessageCommand.MSG_RECIVE_ACK, messageReciveAckContent, messageReciveAckContent.getAppId());
    }

    /**
     * 消息已读。更新会话的seq
     * 通知在线的同步端发送指定command
     * 发送已读回执通知对方（消息发起方）我已读
     * @param messageContent
     */
    public void readMark(MessageReadedContent messageContent) {
        conversationService.messageMarkRead(messageContent);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageContent, messageReadedPack);
        syncToSender(messageReadedPack, messageContent, MessageCommand.MSG_READED_NOTIFY);
        messageProducer.sendToUser(messageContent.getFromId(),MessageCommand.MSG_READED_RECEIPT,messageReadedPack,messageContent.getAppId());

    }

    private void syncToSender(MessageReadedPack pack, MessageReadedContent content, Command command){
        MessageReadedPack messageReadedPack = new MessageReadedPack();
//        BeanUtils.copyProperties(messageReadedContent,messageReadedPack);
        //发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getFromId(),
                command,pack,
                content);
    }

    public void groupReadMark(MessageReadedContent messageReaded) {
        conversationService.messageMarkRead(messageReaded);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded,messageReadedPack);
        syncToSender(messageReadedPack,messageReaded, GroupEventCommand.MSG_GROUP_READED_NOTIFY
        );
        if(!messageReaded.getFromId().equals(messageReaded.getToId())){
            messageProducer.sendToUser(messageReadedPack.getToId(), GroupEventCommand.MSG_GROUP_READED_RECEIPT
                    ,messageReaded,messageReaded.getAppId());
        }
    }

    public ResponseVO syncOfflineMessage(SyncReq req) {

        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperater();

        //获取最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if(!CollectionUtils.isEmpty(set)){
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = o.getScore().longValue();
        }

        List<OfflineMessageContent> respList = new ArrayList<>();
        resp.setMaxSequence(maxSeq);
        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key,
                req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        if(!CollectionUtils.isEmpty(respList)){
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);


    }
}
