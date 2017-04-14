package com.liulishuo.engzo.lingorecorder.processor;

/**
 * Created by wcw on 3/28/17.
 */

public interface AudioProcessor {

    void start() throws Exception;

    void flow(byte[] bytes, int size) throws Exception;

    boolean needExit();

    void end() throws Exception;

    void release();

}
