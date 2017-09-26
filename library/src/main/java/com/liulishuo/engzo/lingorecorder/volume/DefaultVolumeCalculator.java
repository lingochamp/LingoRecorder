package com.liulishuo.engzo.lingorecorder.volume;

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
            for(int i = 0; i < size; i+=2){
                int v1 = chunk[i] & 0xFF;
                int v2 = chunk[i + 1] & 0xFF;
                int temp = v1 + (v2 << 8);
                if (temp >= 0x8000) {
                    temp = 0xffff - temp;
                }
                sumVolume += Math.abs(temp);
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
