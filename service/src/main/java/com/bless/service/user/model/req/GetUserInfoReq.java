package com.bless.service.user.model.req;

import com.bless.common.model.RequestBase;
import lombok.Data;

import java.util.List;

@Data
public class GetUserInfoReq extends RequestBase {

    private List<String> userIds;


}
