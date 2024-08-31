package com.bless.common.model.message;

import lombok.Data;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-31 16:58
 */
@Data
public class DoStoreGroupMessageDto {
    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBody messageBody;
}
