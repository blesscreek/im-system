package com.bless.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.bless.codec.pack.friendship.ApproverFriendRequestPack;
import com.bless.codec.pack.friendship.ReadAllFriendRequestPack;
import com.bless.common.ResponseVO;
import com.bless.common.enums.ApproverFriendRequestStatusEnum;
import com.bless.common.enums.FriendShipErrorCode;
import com.bless.common.enums.command.FriendshipEventCommand;
import com.bless.common.exception.ApplicationException;
import com.bless.service.friendship.dao.ImFriendShipRequestEntity;
import com.bless.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import com.bless.service.friendship.model.req.ApproverFriendRequestReq;
import com.bless.service.friendship.model.req.FriendDto;
import com.bless.service.friendship.model.req.ReadFriendShipRequestReq;
import com.bless.service.friendship.service.ImFriendService;
import com.bless.service.friendship.service.ImFriendShipRequestService;
import com.bless.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImFriendShipRequestServiceImpl implements ImFriendShipRequestService {

    @Autowired
    ImFriendShipRequestMapper imFriendShipRequestMapper;

    @Autowired
    ImFriendService imFriendShipService;
    @Autowired
    MessageProducer messageProducer;
    //A + B
    @Override
    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",appId);
        queryWrapper.eq("from_id",fromId);
        queryWrapper.eq("to_id",dto.getToId());
        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectOne(queryWrapper);

        if(request == null){
            request = new ImFriendShipRequestEntity();
            request.setAddSource(dto.getAddSource());
            request.setAddWording(dto.getAddWording());
            request.setAppId(appId);
            request.setFromId(fromId);
            request.setToId(dto.getToId());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(dto.getRemark());
            request.setCreateTime(System.currentTimeMillis());
            imFriendShipRequestMapper.insert(request);

        }else {
            //修改记录内容 和更新时间
            if(StringUtils.isNotBlank(dto.getAddSource())){
                request.setAddWording(dto.getAddWording());
            }
            if(StringUtils.isNotBlank(dto.getRemark())){
                request.setRemark(dto.getRemark());
            }
            if(StringUtils.isNotBlank(dto.getAddWording())){
                request.setAddWording(dto.getAddWording());
            }
            request.setApproveStatus(0);
            request.setReadStatus(0);
            imFriendShipRequestMapper.updateById(request);
        }
        //发送好友申请的tcp给接收方
        messageProducer.sendToUser(dto.getToId(),
                null, "", FriendshipEventCommand.FRIEND_REQUEST,
                request, appId);

        return ResponseVO.successResponse();
    }
    @Override
    @Transactional
    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req) {

        ImFriendShipRequestEntity imFriendShipRequestEntity = imFriendShipRequestMapper.selectById(req.getId());
        if(imFriendShipRequestEntity == null){
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }

        if(!req.getOperater().equals(imFriendShipRequestEntity.getToId())){
            //只能审批发给自己的好友请求
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setApproveStatus(req.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update.setId(req.getId());
        imFriendShipRequestMapper.updateById(update);


        if(ApproverFriendRequestStatusEnum.AGREE.getCode() == req.getStatus()){
            //同意 ===> 去执行添加好友逻辑
            FriendDto dto = new FriendDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddWording(imFriendShipRequestEntity.getAddWording());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());
            ResponseVO responseVO = imFriendShipService.doAddFriend(req,imFriendShipRequestEntity.getFromId(), dto,req.getAppId());
//            if(!responseVO.isOk()){
////                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return responseVO;
//            }
            if(!responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()){
                return responseVO;
            }
        }
        ApproverFriendRequestPack approverFriendRequestPack = new ApproverFriendRequestPack();
        approverFriendRequestPack.setId(req.getId());
        approverFriendRequestPack.setStatus(req.getStatus());
        messageProducer.sendToUser(imFriendShipRequestEntity.getToId(),req.getClientType(),req.getImei(), FriendshipEventCommand
                .FRIEND_REQUEST_APPROVER,approverFriendRequestPack,req.getAppId());

        return ResponseVO.successResponse();
    }
    @Override
    public ResponseVO getFriendRequest(String fromId, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper();
        query.eq("app_id", appId);
        query.eq("to_id", fromId);

        List<ImFriendShipRequestEntity> requestList = imFriendShipRequestMapper.selectList(query);

        return ResponseVO.successResponse(requestList);
    }

    @Override
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId());

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setReadStatus(1);
        imFriendShipRequestMapper.update(update, query);
        //TCP通知
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
        messageProducer.sendToUser(req.getFromId(),req.getClientType(),req.getImei(),FriendshipEventCommand
                .FRIEND_REQUEST_READ,readAllFriendRequestPack,req.getAppId());

        return ResponseVO.successResponse();
    }


}