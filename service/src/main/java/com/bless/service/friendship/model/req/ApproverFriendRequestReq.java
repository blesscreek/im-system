package com.bless.service.friendship.model.req;

import com.bless.common.model.RequestBase;
import lombok.Data;

@Data
public class ApproverFriendRequestReq extends RequestBase {

    private Long id;

    //1同意 2拒绝
    private Integer status;
}
