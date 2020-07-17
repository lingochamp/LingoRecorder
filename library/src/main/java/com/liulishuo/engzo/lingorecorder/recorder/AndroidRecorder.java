package com.liulishuo.engzo.lingorecorder.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderException;
import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderGetBufferSizeException;
import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderInitException;
import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderReadException;
import com.liulishuo.engzo.lingorecorder.recorder.exception.RecorderStartException;
import com.liulishuo.engzo.lingorecorder.utils.RecorderProperty;

import java.util.UUID;

/**
 * Created by wcw on 4/5/17.
 */

public class AndroidRecorder implements IRecorder {

    private int audioFormat;
    private long payloadSize;
    private int channels;
    private AudioRecord recorder;
    private AcousticEchoCanceler aec;
    private static AudioEffect.Descriptor[] cachedEffects;

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

        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, recorderProperty.getSampleRate(),
                channels, audioFormat, buffSize);

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new RecorderInitException();
        }

        payloadSize = 0;
        recorder.startRecording();

        audioSessionId = recorder.getAudioSessionId();

        boolean aecAvailable = isEffectTypeAvailable(AudioEffect.EFFECT_TYPE_AEC,
                AOSP_ACOUSTIC_ECHO_CANCELER);
        Log.d("AndroidRecorder", "AEC Available = " + aecAvailable);

        if (aecAvailable) {
            aec = AcousticEchoCanceler.create(audioSessionId);
            if (aec == null || aec.setEnabled(true) != AudioEffect.SUCCESS) {
                Log.e("AndroidRecorder", "enable aec fail");
            } else {
                Log.e("AndroidRecorder", "enable aec success");
            }
        }

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
        if (aec != null) {
            aec.release();
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

    public volatile static int audioSessionId = -1;

    private static final UUID AOSP_ACOUSTIC_ECHO_CANCELER =
            UUID.fromString("bb392ec0-8d4d-11e0-a896-0002a5d5c51b");

    private static boolean isEffectTypeAvailable(UUID effectType, UUID blackListedUuid) {
        AudioEffect.Descriptor[] effects = getAvailableEffects();
        if (effects == null) {
            return false;
        }
        for (AudioEffect.Descriptor d : effects) {
            if (d.type.equals(effectType)) {
                return !d.uuid.equals(blackListedUuid);
            }
        }
        return false;
    }

    private static @Nullable
    AudioEffect.Descriptor[] getAvailableEffects() {
        if (cachedEffects != null) {
            return cachedEffects;
        }
        // The caching is best effort only - if this method is called from several
        // threads in parallel, they may end up doing the underlying OS call
        // multiple times. It's normally only called on one thread so there's no
        // real need to optimize for the multiple threads case.
        cachedEffects = AudioEffect.queryEffects();
        return cachedEffects;
    }

}
