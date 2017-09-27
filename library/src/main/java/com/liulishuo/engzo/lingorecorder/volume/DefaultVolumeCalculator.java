package com.liulishuo.engzo.lingorecorder.volume;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

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
            byte[] twoByte = new byte[2];
            for(int i = 0; i < size; i+=2){
                twoByte[0] = chunk[i];
                twoByte[1] = chunk[i + 1];
                final ByteBuffer byteBuffer = ByteBuffer.wrap(twoByte);
                final ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                sumVolume += Math.abs(shortBuffer.get(0));
            }
            avgVolume = sumVolume / (size / 2);
        } else {
            for (int i = 0; i < size; i++) {
                sumVolume += Math.abs(chunk[i]);
            }
            avgVolume = sumVolume / size;
        }
        return 20 * Math.log10(avgVolume);
    }
}
