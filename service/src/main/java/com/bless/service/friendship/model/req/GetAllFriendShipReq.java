package com.bless.service.friendship.model.req;

import com.bless.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GetAllFriendShipReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;
}
