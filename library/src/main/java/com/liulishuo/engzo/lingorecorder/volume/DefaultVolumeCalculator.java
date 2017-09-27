package com.liulishuo.engzo.lingorecorder.volume;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by rantianhua on 2017/9/26.
 * provide a default volume calculator
 */

public class DefaultVolumeCalculator implements IVolumeCalculator {

    /**
     *
     * @param chunk record chunk
     * @param size size of chunk
     * @param bitsPerSample bits per sample
     * @return the volume decibels 0 - 90
     */
    @Override
    public double onAudioChunk(byte[] chunk, int size, int bitsPerSample) {
        double sumVolume = 0.0;
        double avgVolume;
        if (bitsPerSample == 16) {
            final ByteBuffer byteBuffer = ByteBuffer.wrap(chunk, 0, size);
            final short[] buf = new short[size / 2];
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buf);
            for (short b : buf) {
                sumVolume += Math.abs(b);
            }
            avgVolume = sumVolume / buf.length;
        } else {
            for (int i = 0; i < size; i++) {
                sumVolume += Math.abs(chunk[i]);
            }
            avgVolume = sumVolume / size;
        }
        return 20 * Math.log10(avgVolume);
    }
}
