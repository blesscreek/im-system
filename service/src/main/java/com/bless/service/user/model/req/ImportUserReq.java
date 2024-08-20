package com.bless.service.user.model.req;

import com.bless.common.model.RequestBase;
import com.bless.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-20 11:27
 */
@Data
public class ImportUserReq extends RequestBase {
    private List<ImUserDataEntity> userData;
}
