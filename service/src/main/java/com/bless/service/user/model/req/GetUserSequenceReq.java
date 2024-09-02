package com.bless.service.user.model.req;

import com.bless.common.model.RequestBase;
import lombok.Data;

@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}
