package com.bless.service.conversation.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author:
 * @description: 聊天会话类
 **/
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    //会话id 0_fromId_toId
    private String conversationId;

    //会话类型 0私聊 1群聊
    private Integer conversationType;

    private String fromId;

    private String toId;

    //是否免打扰
    private int isMute;

    //是否置顶
    private int isTop;

    private Long sequence;

    //记录已读到哪条sequence
    private Long readedSequence;

    private Integer appId;
}
