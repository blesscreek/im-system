package com.bless.tcp.feign;

import com.bless.common.ResponseVO;
import com.bless.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

/**
 * @Author bless
 * @Version 1.0
 * @Description TODO
 * @Date 2024-08-30 16:03
 */
public interface FeignMessageService {

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSendMessage(CheckSendMessageReq o);
}
