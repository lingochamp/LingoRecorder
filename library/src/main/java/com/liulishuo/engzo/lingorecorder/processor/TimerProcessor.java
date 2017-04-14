package com.liulishuo.engzo.lingorecorder.processor;

public class TimerProcessor implements AudioProcessor {

    private long mTimeInMills = Integer.MAX_VALUE;
    private long startTime = 0;

    private boolean needExit = false;

    public TimerProcessor(long timeInMills) {
        mTimeInMills = timeInMills;
    }

    @Override
    public void start() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void flow(byte[] bytes, int size) {
        needExit = System.currentTimeMillis() - startTime >= mTimeInMills;
    }

    @Override
    public boolean needExit() {
        return needExit;
    }

    @Override
    public void end() {

    }

    @Override
    public void release() {

    }
}
