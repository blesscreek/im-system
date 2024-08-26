package com.bless.common.model;

import lombok.Data;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-24 11:56
 */
@Data
public class UserSession {
    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 端的标识
     */
    private Integer clientType;

    //sdk 版本号
    private Integer version;

    //连接状态 1=在线 2=离线
    private Integer connectState;

    //服务id
    private Integer brokerId;

    //服务器ip
    private String brokerHost;

    private String imei;
}
