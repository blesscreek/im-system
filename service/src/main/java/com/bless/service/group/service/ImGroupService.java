package com.bless.service.group.service;

import com.bless.common.ResponseVO;
import com.bless.common.model.SyncReq;
import com.bless.service.group.dao.ImGroupEntity;
import com.bless.service.group.model.req.*;

/**
 * @Author bless
 * @Version 1.0
 * @Description TODO
 * @Date 2024-08-21 10:53
 */
public interface ImGroupService {
    public ResponseVO importGroup(ImportGroupReq req);
    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);
    public ResponseVO createGroup(CreateGroupReq req);
    public ResponseVO getGroup(GetGroupReq req);
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req);
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req);
    public ResponseVO destroyGroup(DestroyGroupReq req);
    public ResponseVO transferGroup(TransferGroupReq req);
    public ResponseVO muteGroup(MuteGroupReq req);

    public ResponseVO syncJoinedGroupList(SyncReq req);

    Long getUserGroupMaxSeq(String userId, Integer appId);
}
