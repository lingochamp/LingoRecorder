package com.liulishuo.engzo.lingorecorder.volume;

/**
 * Created by rantianhua on 2017/9/26.
 * manage volume calculator,
 * the {@link #volumeCalculator} can be changed
 */

public class VolumePlugin {

    private static volatile IVolumeCalculator volumeCalculator;

    private static VolumePlugin INSTANCE;

    private VolumePlugin() {
        volumeCalculator = new DefaultVolumeCalculator();
    }

    public synchronized static VolumePlugin getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VolumePlugin();
        }
        return INSTANCE;
    }

    public void setVolumeCalculator(IVolumeCalculator volumeCalculator) {
        VolumePlugin.volumeCalculator = volumeCalculator;
    }

    public IVolumeCalculator getVolumeCalculator() {
        return volumeCalculator;
    }
}
