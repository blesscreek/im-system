package com.bless.common.model.message;

import lombok.Data;

@Data
public class CheckSendMessageReq {

    private String fromId;

    private String toId;

    private Integer appId;

    private Integer command;

}
