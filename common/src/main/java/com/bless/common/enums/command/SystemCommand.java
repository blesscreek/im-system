package com.bless.common.enums.command;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-23 21:37
 */

public enum SystemCommand implements Command{
    //心跳 9999
    PING(0x270f),


    /**
     * 登录 9000
     */
    LOGIN(0x2328),

    //登出  9003
    LOGOUT(0x232b),

    ;

    private int command;

    SystemCommand(int command){
        this.command=command;
    }

    public int getCommand() {
        return command;
    }
}