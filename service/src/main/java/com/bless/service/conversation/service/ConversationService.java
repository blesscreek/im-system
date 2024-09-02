package com.bless.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.bless.codec.pack.conversation.DeleteConversationPack;
import com.bless.codec.pack.conversation.UpdateConversationPack;
import com.bless.common.ResponseVO;
import com.bless.common.config.AppConfig;
import com.bless.common.constant.Constants;
import com.bless.common.enums.ConversationErrorCode;
import com.bless.common.enums.ConversationTypeEnum;
import com.bless.common.enums.command.ConversationEventCommand;
import com.bless.common.model.ClientInfo;
import com.bless.common.model.SyncReq;
import com.bless.common.model.SyncResp;
import com.bless.common.model.message.MessageReadedContent;
import com.bless.service.conversation.dao.ImConversationSetEntity;
import com.bless.service.conversation.dao.mapper.ImConversationSetMapper;
import com.bless.service.conversation.model.DeleteConversationReq;
import com.bless.service.conversation.model.UpdateConversationReq;
import com.bless.service.seq.RedisSeq;
import com.bless.service.utils.MessageProducer;
import com.bless.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-31 21:12
 */
@Service
public class ConversationService {
    @Autowired
    ImConversationSetMapper imConversationSetMapper;
    @Autowired
    AppConfig appConfig;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    WriteUserSeq writeUserSeq;

    public String convertConversationId(Integer type,String fromId,String toId){
        return type + "_" + fromId + "_" + toId;
    }

    public void  messageMarkRead(MessageReadedContent messageReadedContent){

        String toId = messageReadedContent.getToId();
        if(messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()){
            toId = messageReadedContent.getGroupId();
        }

        String conversationId = convertConversationId(messageReadedContent.getConversationType(),
                messageReadedContent.getFromId(), messageReadedContent.getToId());
        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id",conversationId);
        query.eq("app_id",messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if(imConversationSetEntity == null){
            imConversationSetEntity = new ImConversationSetEntity();
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent,imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(),
                    messageReadedContent.getFromId(),Constants.SeqConstants.Conversation,seq);

        }else{
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(),
                    messageReadedContent.getFromId(),Constants.SeqConstants.Conversation,seq);
        }
    }


    public ResponseVO deleteConversation(DeleteConversationReq req) {
        //置顶 有免打扰
//        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("conversation_id",req.getConversationId());
//        queryWrapper.eq("app_id",req.getAppId());
//        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
//        if(imConversationSetEntity != null){
//            imConversationSetEntity.setIsMute(0);
//            imConversationSetEntity.setIsTop(0);
//            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
//        }
        if(appConfig.getDeleteConversationSyncMode() == 1){
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    pack,new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }
        return ResponseVO.successResponse();
    }

    /**
     * @description: 更新会话 置顶or免打扰
     */
    public ResponseVO updateConversation(UpdateConversationReq req){

        if(req.getIsTop() == null && req.getIsMute() == null){
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id",req.getConversationId());
        queryWrapper.eq("app_id",req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if(imConversationSetEntity != null){
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);
           if(req.getIsTop() != null){
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if(req.getIsMute() != null){
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
             imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(),
                    Constants.SeqConstants.Conversation, seq);

            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack,new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }
        return ResponseVO.successResponse();
    }

    public ResponseVO syncConversationSet(SyncReq req) {
        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncResp<ImConversationSetEntity> resp = new SyncResp<>();
        //seq > req.getseq limit maxLimit
        QueryWrapper<ImConversationSetEntity> queryWrapper =
                new QueryWrapper<>();
        queryWrapper.eq("from_id",req.getOperater());
        queryWrapper.gt("sequence",req.getLastSequence());
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("sequence");
        List<ImConversationSetEntity> list = imConversationSetMapper
                .selectList(queryWrapper);

        if(!CollectionUtils.isEmpty(list)){
            ImConversationSetEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);

    }
}
