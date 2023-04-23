package com.catzhang.im.tcp.consume.process;

/**
 * @author crazycatzhang
 */
public class ProcessFactory {

    private static BaseProcess defaultProcess;

    static {
        defaultProcess = new BaseProcess() {
            @Override
            protected void processBefore() {

            }

            @Override
            protected void processAfter() {

            }
        };
    }

    public static BaseProcess getMessageProcess(Integer command) {
        return defaultProcess;
    }

}
