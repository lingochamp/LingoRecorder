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

    private long payloadSize;

    private static final int sampleRate = 16000;
    private static final int bitsPerSample = 16;
    private static final int nChannels = 1;

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
        payloadSize = 0;
    }

    @Override
    public int read(@NonNull byte[] bytes, int buffSize) throws Exception {
        int count = fis.read(bytes, 0, buffSize);
        payloadSize += count;
        return count;
    }

    @Override
    public void release() {
        try {
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getDurationInMills() {
        return (long) (payloadSize * 8.0 * 1000 / bitsPerSample / sampleRate / nChannels );
    }
}
