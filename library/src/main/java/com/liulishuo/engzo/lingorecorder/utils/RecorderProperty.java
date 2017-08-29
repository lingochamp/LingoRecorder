package com.liulishuo.engzo.lingorecorder.utils;

/**
 * Created by rantianhua on 17/8/29.
 * hold properties for a recorder
 */

public class RecorderProperty {

    private int sampleRate;
    private int channels;
    private int bitsPerSample;
    /**
     * use a wav file as audio source
     * used for {@link com.liulishuo.engzo.lingorecorder.recorder.WavFileRecorder}
     */
    private String wavFile;
    /**
     * output file as record result
     */
    private String outputFile;

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

    public String getWavFile() {
        return wavFile;
    }

    public RecorderProperty setWavFile(String wavFile) {
        this.wavFile = wavFile;
        return this;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public RecorderProperty setOutputFile(String outputFile) {
        this.outputFile = outputFile;
        return this;
    }
}
