package com.bless.message.model;


import com.bless.common.model.message.MessageContent;
import com.bless.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
