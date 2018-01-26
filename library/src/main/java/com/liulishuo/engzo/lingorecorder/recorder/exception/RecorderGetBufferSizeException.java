package com.liulishuo.engzo.lingorecorder.recorder.exception;

/**
 * Created by wcw on 1/26/18.
 */

public class RecorderGetBufferSizeException extends RecorderException {

    public RecorderGetBufferSizeException(int error) {
        super("recorder get buffer size error " + error);
    }
}
