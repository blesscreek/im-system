package com.bless.service.friendship.service;

import com.bless.common.ResponseVO;
import com.bless.service.friendship.dao.ImFriendShipGroupEntity;
import com.bless.service.friendship.model.req.AddFriendShipGroupReq;
import com.bless.service.friendship.model.req.DeleteFriendShipGroupReq;

public interface ImFriendShipGroupService {

    public ResponseVO addGroup(AddFriendShipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

}
