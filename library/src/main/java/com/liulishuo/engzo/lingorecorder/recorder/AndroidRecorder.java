package com.liulishuo.engzo.lingorecorder.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;

import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderException;
import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderGetBufferSizeException;
import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderInitException;
import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderReadException;
import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderStartException;
import com.liulishuo.engzo.lingorecorder.utils.RecorderProperty;

/**
 * Created by wcw on 4/5/17.
 */

public class AndroidRecorder implements IRecorder {

    private int audioFormat;
    private long payloadSize;
    private int channels;
    private AudioRecord recorder;

    private final RecorderProperty recorderProperty;

    public AndroidRecorder(final RecorderProperty recorderProperty) {
        this.recorderProperty = recorderProperty;
        if (this.recorderProperty.getBitsPerSample() == 16) {
            audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        } else if (this.recorderProperty.getBitsPerSample() == 8) {
            audioFormat = AudioFormat.ENCODING_PCM_8BIT;
        } else {
            throw new RecorderException(
                    "unsupported bitsPerSample: " + this.recorderProperty.getBitsPerSample());
        }
        if (this.recorderProperty.getChannels() == 1) {
            this.channels = AudioFormat.CHANNEL_IN_MONO;
        } else if (this.recorderProperty.getChannels() == 2) {
            this.channels = AudioFormat.CHANNEL_IN_STEREO;
        } else {
            throw new RecorderException(
                    "unsupported channel: " + this.recorderProperty.getChannels());
        }
    }

    @Override
    public int getBufferSize() {
        int ret = AudioRecord.getMinBufferSize(recorderProperty.getSampleRate(), channels,
                audioFormat);
        if (ret > 0) {
            return 2 * ret;
        } else {
            throw new RecorderGetBufferSizeException(ret);
        }
    }

    @Override
    public void startRecording() throws Exception {
        int buffSize = getBufferSize();

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, recorderProperty.getSampleRate(),
                channels, audioFormat, buffSize);

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new RecorderInitException();
        }

        payloadSize = 0;
        recorder.startRecording();

        if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            throw new RecorderStartException();
        }
    }

    @Override
    public int read(@NonNull byte[] bytes, int buffSize) throws Exception {
        int read = recorder.read(bytes, 0, buffSize);
        if (read < 0) {
            throw new RecorderReadException(read);
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
        return (long) (payloadSize * 8.0 * 1000 / recorderProperty.getBitsPerSample()
                / recorderProperty.getSampleRate() / recorderProperty.getChannels());
    }

    @Override
    public RecorderProperty getRecordProperty() {
        return recorderProperty;
    }
}
