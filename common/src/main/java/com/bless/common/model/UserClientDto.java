package com.bless.common.model;

import lombok.Data;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-24 16:00
 */
@Data
public class UserClientDto {
    private Integer appId;

    private Integer clientType;

    private String userId;

    private String imei;

}
