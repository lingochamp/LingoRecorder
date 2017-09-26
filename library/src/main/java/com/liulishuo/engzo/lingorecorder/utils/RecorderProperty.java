package com.liulishuo.engzo.lingorecorder.utils;

/**
 * Created by rantianhua on 17/8/29.
 * hold properties for a recorder
 */

public class RecorderProperty {

    private int sampleRate;
    private int channels;
    private int bitsPerSample;

    public RecorderProperty() {
        setSampleRate(16000);
        setChannels((short) 1);
        setBitsPerSample(16);
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public RecorderProperty setBitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
        return this;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public RecorderProperty setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public int getChannels() {
        return channels;
    }

    public RecorderProperty setChannels(int channels) {
        this.channels = channels;
        return this;
    }
}
