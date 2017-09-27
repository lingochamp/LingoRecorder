package com.liulishuo.engzo.lingorecorder.volume;

/**
 * Created by rantianhua on 2017/9/26.
 * calculate volume
 */

public interface IVolumeCalculator {

    double onAudioChunk(byte[] chunk, int size, int bitsPerSample);
}
