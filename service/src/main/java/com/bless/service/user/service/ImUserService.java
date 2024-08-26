package com.bless.service.user.service;

import com.bless.common.ResponseVO;
import com.bless.service.user.dao.ImUserDataEntity;
import com.bless.service.user.model.req.*;
import com.bless.service.user.model.resp.GetUserInfoResp;

/**
 * @Author bless
 * @Version 1.0
 * @Description TODO
 * @Date 2024-08-20 11:07
 */
public interface ImUserService {
     public ResponseVO importUser(ImportUserReq req);
     public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

     public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId , Integer appId);

     public ResponseVO deleteUser(DeleteUserReq req);

     public ResponseVO modifyUserInfo(ModifyUserInfoReq req);

     public ResponseVO login(LoginReq req);
}
