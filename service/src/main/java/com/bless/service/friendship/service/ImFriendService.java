package com.bless.service.friendship.service;

import com.bless.common.ResponseVO;
import com.bless.common.model.RequestBase;
import com.bless.common.model.SyncReq;
import com.bless.service.friendship.model.req.*;

import java.util.List;

public interface ImFriendService {

    public ResponseVO importFriendShip(ImporFriendShipReq req);

    public ResponseVO addFriend(AddFriendReq req);

    public ResponseVO updateFriend(UpdateFriendReq req);

    public ResponseVO deleteFriend(DeleteFriendReq req);

    public ResponseVO deleteAllFriend(DeleteFriendReq req);

    public ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    public ResponseVO getRelation(GetRelationReq req);

    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId);

    public ResponseVO checkFriendship(CheckFriendShipReq req);

    public ResponseVO addBlack(AddFriendShipBlackReq req);

    public ResponseVO deleteBlack(DeleteBlackReq req);

    public ResponseVO checkBlack(CheckFriendShipReq req);


    public ResponseVO syncFriendshipList(SyncReq req);

    public List<String> getAllFriendId(String userId, Integer appId);
}
