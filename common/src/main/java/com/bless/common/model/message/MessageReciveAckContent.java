package com.bless.common.model.message;

import com.bless.common.model.ClientInfo;
import lombok.Data;

@Data
public class MessageReciveAckContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;


}
