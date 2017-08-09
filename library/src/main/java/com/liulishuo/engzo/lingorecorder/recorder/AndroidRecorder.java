package com.liulishuo.engzo.lingorecorder.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;

/**
 * Created by wcw on 4/5/17.
 */

public class AndroidRecorder implements IRecorder {

    private int sampleRate;
    private int channels;
    private int audioFormat;
    private long payloadSize;
    private AudioRecord recorder;
    private int bitsPerSample;
    private int nChannels;

    public AndroidRecorder(int sampleRate, int channels, int bitsPerSample) {
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.nChannels = channels;
        if (bitsPerSample == 16) {
            audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        } else if (bitsPerSample == 8) {
            audioFormat = AudioFormat.ENCODING_PCM_8BIT;
        }
        if (channels == 1) {
            this.channels = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        } else if (channels == 2) {
            this.channels = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        }
    }

    @Override
    public int getBufferSize() {
        return 2 * AudioRecord.getMinBufferSize(sampleRate, channels, audioFormat);
    }

    @Override
    public void startRecording() throws Exception {
        int buffSize = getBufferSize();
        if (AudioRecord.ERROR == getBufferSize())
            throw new RecordException("get buffer size error");

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channels,
                        audioFormat, buffSize);

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED)
            throw new RecordException("init Android audioRecorder error");

        payloadSize = 0;
        recorder.startRecording();
    }

    @Override
    public int read(@NonNull byte[] bytes, int buffSize) throws Exception {
        int read = recorder.read(bytes, 0, buffSize);
        if (read < 0) {
            throw new RecordException("recorder read error " + read);
        }
        payloadSize += read;
        return read;
    }

    @Override
    public void release() {
        if (recorder != null) {
            recorder.release();
        }
    }

    @Override
    public long getDurationInMills() {
        return (long) (payloadSize * 8.0 * 1000 / bitsPerSample / sampleRate / nChannels);
    }

    private class RecordException extends Exception {
        RecordException(String message) {
            super(message);
        }
    }
}
