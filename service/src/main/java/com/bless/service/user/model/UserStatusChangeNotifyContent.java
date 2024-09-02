package com.bless.service.user.model;

import com.bless.common.model.ClientInfo;
import lombok.Data;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-09-02 16:57
 */
@Data
public class UserStatusChangeNotifyContent extends ClientInfo {

    private String userId;

    //服务端状态 1上线 2离线
    private Integer status;
}
