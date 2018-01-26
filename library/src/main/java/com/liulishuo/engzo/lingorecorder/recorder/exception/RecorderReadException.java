package com.liulishuo.engzo.lingorecorder.recorder.exception;

/**
 * Created by wcw on 1/26/18.
 */

public class RecorderReadException extends RecorderException {

    public RecorderReadException(int error) {
        super("recorder read error " + error);
    }
}
