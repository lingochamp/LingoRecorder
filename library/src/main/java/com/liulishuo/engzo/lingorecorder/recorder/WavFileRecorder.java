package com.liulishuo.engzo.lingorecorder.recorder;

import android.support.annotation.NonNull;

import com.liulishuo.engzo.lingorecorder.utils.LOG;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by wcw on 4/5/17.
 */

public class WavFileRecorder implements IRecorder {

    private String filePath;
    private FileInputStream fis;

    public WavFileRecorder(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int getBufferSize() {
        return 1024;
    }

    @Override
    public void startRecording() throws Exception {
        fis = new FileInputStream(filePath);
        long skip = fis.skip(44);
        LOG.d("skip size = " + skip);
    }

    @Override
    public int read(@NonNull byte[] bytes, int buffSize) throws Exception {
        return fis.read(bytes, 0, buffSize);
    }

    @Override
    public void release() {
        try {
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
