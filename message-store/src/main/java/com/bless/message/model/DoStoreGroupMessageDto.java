package com.bless.message.model;

import com.bless.common.model.message.GroupChatMessageContent;
import com.bless.message.dao.ImMessageBodyEntity;
import lombok.Data;

@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
