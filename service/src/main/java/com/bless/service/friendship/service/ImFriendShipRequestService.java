package com.bless.service.friendship.service;

import com.bless.common.ResponseVO;
import com.bless.service.friendship.model.req.ApproverFriendRequestReq;
import com.bless.service.friendship.model.req.FriendDto;
import com.bless.service.friendship.model.req.ReadFriendShipRequestReq;

public interface ImFriendShipRequestService {

    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId);

    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req);

    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    public ResponseVO getFriendRequest(String fromId, Integer appId);
}
