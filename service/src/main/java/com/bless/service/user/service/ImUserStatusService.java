package com.bless.service.user.service;

import com.bless.service.user.model.UserStatusChangeNotifyContent;
import com.bless.service.user.model.req.PullFriendOnlineStatusReq;
import com.bless.service.user.model.req.PullUserOnlineStatusReq;
import com.bless.service.user.model.req.SetUserCustomerStatusReq;
import com.bless.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.bless.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-09-02 17:00
 */

public interface ImUserStatusService {
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomerStatus(SetUserCustomerStatusReq req);

    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);
}
