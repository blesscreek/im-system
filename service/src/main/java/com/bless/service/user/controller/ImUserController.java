package com.bless.service.user.controller;

import com.bless.common.ClientType;
import com.bless.common.ResponseVO;
import com.bless.common.route.RouteHandle;
import com.bless.common.route.RouteInfo;
import com.bless.common.utils.RouteInfoParseUtil;
import com.bless.service.user.model.req.DeleteUserReq;
import com.bless.service.user.model.req.ImportUserReq;
import com.bless.service.user.model.req.LoginReq;
import com.bless.service.user.service.ImUserService;
import com.bless.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {
    @Autowired
    ImUserService imUserService;
    @Autowired
    RouteHandle routeHandle;

    @Autowired
    ZKit zKit;


    @RequestMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req) {
//        req.setAppId(appId);
        return imUserService.importUser(req);
    }


    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req) {
        return imUserService.deleteUser(req);
    }

    /**
     * @description im的登录接口，返回im地址
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);

        ResponseVO login = imUserService.login(req);
        if (login.isOk()) {
            List<String> allNode = new ArrayList<>();
            if (req.getClientType() == ClientType.WEB.getCode()) {
                allNode = zKit.getAllWebNode();
            } else {
                allNode = zKit.getAllTcpNode();
            }
            String s = routeHandle.routeServer(allNode, req.getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }

        return ResponseVO.errorResponse();
    }

//    @RequestMapping("/getUserSequence")
//    public ResponseVO getUserSequence(@RequestBody @Validated
//                                              GetUserSequenceReq req, Integer appId) {
//        req.setAppId(appId);
//        return imUserService.getUserSequence(req);
//    }
//
//    @RequestMapping("/subscribeUserOnlineStatus")
//    public ResponseVO subscribeUserOnlineStatus(@RequestBody @Validated
//                                                        SubscribeUserOnlineStatusReq req, Integer appId,String identifier) {
//        req.setAppId(appId);
//        req.setOperater(identifier);
//        imUserStatusService.subscribeUserOnlineStatus(req);
//        return ResponseVO.successResponse();
//    }
//
//    @RequestMapping("/setUserCustomerStatus")
//    public ResponseVO setUserCustomerStatus(@RequestBody @Validated
//                                                        SetUserCustomerStatusReq req, Integer appId,String identifier) {
//        req.setAppId(appId);
//        req.setOperater(identifier);
//        imUserStatusService.setUserCustomerStatus(req);
//        return ResponseVO.successResponse();
//    }
//
//    @RequestMapping("/queryFriendOnlineStatus")
//    public ResponseVO queryFriendOnlineStatus(@RequestBody @Validated
//                                                      PullFriendOnlineStatusReq req, Integer appId,String identifier) {
//        req.setAppId(appId);
//        req.setOperater(identifier);
//        return ResponseVO.successResponse(imUserStatusService.queryFriendOnlineStatus(req));
//    }
//
//    @RequestMapping("/queryUserOnlineStatus")
//    public ResponseVO queryUserOnlineStatus(@RequestBody @Validated
//                                                      PullUserOnlineStatusReq req, Integer appId,String identifier) {
//        req.setAppId(appId);
//        req.setOperater(identifier);
//        return ResponseVO.successResponse(imUserStatusService.queryUserOnlineStatus(req));
//    }
//


}
