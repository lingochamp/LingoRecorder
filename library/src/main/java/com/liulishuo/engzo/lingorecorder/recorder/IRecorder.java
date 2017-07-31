package com.liulishuo.engzo.lingorecorder.recorder;

import android.support.annotation.NonNull;

/**
 * Created by wcw on 4/5/17.
 */

public interface IRecorder {

    int getBufferSize();

    void startRecording() throws Throwable;

    int read(@NonNull byte[] bytes, int buffSize) throws Throwable;

    void release();

    long getDurationInMills();
}
