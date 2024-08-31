package com.bless.common.model.message;

import lombok.Data;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-31 9:32
 */
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBody messageBody;

}
