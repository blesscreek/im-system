package com.bless.service.group.service;

import com.bless.common.ResponseVO;
import com.bless.service.group.model.req.*;
import com.bless.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;
import java.util.List;

/**
 * @Author bless
 * @Version 1.0
 * @Description TODO
 * @Date 2024-08-21 10:54
 */
public interface ImGroupMemberService {
    public ResponseVO importGroupMember(ImportGroupMemberReq req);
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId);
    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId);
    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);
    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId);
    public ResponseVO addMember(AddGroupMemberReq req);
    public ResponseVO removeMember(RemoveGroupMemberReq req);
    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId);
    public ResponseVO updateGroupMember(UpdateGroupMemberReq req);
    public ResponseVO speak(SpeaMemberReq req);

    public List<String> getGroupMemberId(String groupId, Integer appId);

    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId);


}
