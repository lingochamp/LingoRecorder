package com.liulishuo.engzo.lingorecorder.processor;

import com.liulishuo.engzo.lingorecorder.utils.RecorderProperty;

public class TimerProcessor implements AudioProcessor {

    private long mTimeInMills = Integer.MAX_VALUE;

    private RecorderProperty mRecorderProperty;

    private long payloadSize = 0L;

    public TimerProcessor(RecorderProperty recorderProperty, long timeInMills) {
        mRecorderProperty = recorderProperty;
        mTimeInMills = timeInMills;
    }

    public void setTimeInMills(long timeInMills) {
        this.mTimeInMills = timeInMills;
    }

    @Override
    public void start() {
        payloadSize = 0;
    }

    @Override
    public void flow(byte[] bytes, int size) {
        payloadSize += size;
    }

    @Override
    public boolean needExit() {
        long payloadSizeInBits = payloadSize * 8;
        long durationInMills = (long)
                (payloadSizeInBits * 1000.0 / mRecorderProperty.getBitsPerSample()
                        / mRecorderProperty.getSampleRate() / mRecorderProperty.getChannels());
        return durationInMills >= mTimeInMills;
    }

    @Override
    public void end() {

    }

    @Override
    public void release() {

    }
}