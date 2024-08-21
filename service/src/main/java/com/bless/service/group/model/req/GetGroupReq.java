package com.bless.service.group.model.req;

import com.bless.common.model.RequestBase;
import lombok.Data;

@Data
public class GetGroupReq extends RequestBase {

    private String groupId;

}
