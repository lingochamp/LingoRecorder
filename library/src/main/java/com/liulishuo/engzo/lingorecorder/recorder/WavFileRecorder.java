package com.liulishuo.engzo.lingorecorder.recorder;

import android.support.annotation.NonNull;

import com.liulishuo.engzo.lingorecorder.utils.LOG;
import com.liulishuo.engzo.lingorecorder.utils.RecorderProperty;

import java.io.FileInputStream;

/**
 * Created by wcw on 4/5/17.
 */

public class WavFileRecorder implements IRecorder {

    private FileInputStream fis;
    private long payloadSize;

    private final String filePath;
    private final RecorderProperty recorderProperty;

    public WavFileRecorder(String filePath, RecorderProperty recorderProperty) {
        this.filePath = filePath;
        this.recorderProperty = recorderProperty;
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
        return (long) (payloadSize * 8.0 * 1000 / recorderProperty.getBitsPerSample()
                / recorderProperty.getSampleRate() / recorderProperty.getChannels());
    }

    @Override
    public RecorderProperty getRecordProperty() {
        return recorderProperty;
    }
}
