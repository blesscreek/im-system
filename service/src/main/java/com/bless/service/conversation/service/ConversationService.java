package com.bless.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bless.codec.pack.conversation.DeleteConversationPack;
import com.bless.codec.pack.conversation.UpdateConversationPack;
import com.bless.common.ResponseVO;
import com.bless.common.config.AppConfig;
import com.bless.common.constant.Constants;
import com.bless.common.enums.ConversationErrorCode;
import com.bless.common.enums.ConversationTypeEnum;
import com.bless.common.enums.command.ConversationEventCommand;
import com.bless.common.model.ClientInfo;
import com.bless.common.model.message.MessageReadedContent;
import com.bless.service.conversation.dao.ImConversationSetEntity;
import com.bless.service.conversation.dao.mapper.ImConversationSetMapper;
import com.bless.service.conversation.model.DeleteConversationReq;
import com.bless.service.conversation.model.UpdateConversationReq;
import com.bless.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent,imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.insert(imConversationSetEntity);
        }else{
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);

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

            if(req.getIsMute() != null){
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if(req.getIsMute() != null){
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);

            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack,new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }
        return ResponseVO.successResponse();
    }

}
