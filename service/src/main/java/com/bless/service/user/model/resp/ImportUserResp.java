package com.bless.service.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-20 12:07
 */
@Data
public class ImportUserResp {
    private List<String> successId;
    private List<String> errorId;
}
