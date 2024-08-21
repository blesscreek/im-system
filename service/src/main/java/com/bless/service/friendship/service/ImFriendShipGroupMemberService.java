package com.bless.service.friendship.service;

import com.bless.common.ResponseVO;
import com.bless.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.bless.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

public interface ImFriendShipGroupMemberService {

    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    public int doAddGroupMember(Long groupId, String toId);

    public int clearGroupMember(Long groupId);
}
