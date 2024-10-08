package com.bless.service.message.controller;

import com.bless.common.ResponseVO;
import com.bless.common.model.SyncReq;
import com.bless.common.model.message.CheckSendMessageReq;
import com.bless.service.message.model.req.SendMessageReq;
import com.bless.service.message.service.MessageSyncService;
import com.bless.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-29 21:41
 */
@RestController
@RequestMapping("v1/message")
public class MessageController {
    @Autowired
    P2PMessageService p2PMessageService;
    @Autowired
    MessageSyncService messageSyncService;
    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId)  {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {
        return p2PMessageService.imServerPermissionCheck(req.getFromId(),req.getToId()
                ,req.getAppId());
    }
    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody
                                         @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req);
    }

}
