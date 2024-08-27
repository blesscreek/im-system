package com.bless.tcp.reciver.process;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-27 18:00
 */

public class ProcessFactory {
    private static BaseProcess defaultProcess;

    static {
        defaultProcess = new BaseProcess() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }
    public static BaseProcess getMessageProcess(Integer command) {
        return defaultProcess;
    }
}
