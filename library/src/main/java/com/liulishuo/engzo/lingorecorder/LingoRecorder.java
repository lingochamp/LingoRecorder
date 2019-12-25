package com.liulishuo.engzo.lingorecorder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.processor.WavProcessor;
import com.liulishuo.engzo.lingorecorder.recorder.AndroidRecorder;
import com.liulishuo.engzo.lingorecorder.recorder.IRecorder;
import com.liulishuo.engzo.lingorecorder.recorder.WavFileRecorder;
import com.liulishuo.engzo.lingorecorder.utils.LOG;
import com.liulishuo.engzo.lingorecorder.utils.RecorderProperty;
import com.liulishuo.engzo.lingorecorder.utils.WrapBuffer;
import com.liulishuo.engzo.lingorecorder.volume.DefaultVolumeCalculator;
import com.liulishuo.engzo.lingorecorder.volume.IVolumeCalculator;
import com.liulishuo.engzo.lingorecorder.volume.OnVolumeListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by wcw on 3/28/17.
 */

public class LingoRecorder {

    private Map<String, AudioProcessor> audioProcessorMap = new HashMap<>();
    private InternalRecorder internalRecorder;

    private final static int MESSAGE_RECORD_STOP = 1;
    private final static int MESSAGE_PROCESS_STOP = 2;
    private final static int MESSAGE_VOLUME = 4;
    private final static String KEY_DURATION = "duration";
    private final static String KEY_FILEPATH = "filePath";

    private OnRecordStopListener onRecordStopListener;
    private OnProcessStopListener onProcessStopListener;
    private OnVolumeListener onVolumeListener;
    private IVolumeCalculator volumeCalculator;

    private boolean available = true;
    private boolean isProcessing = false;

    private final RecorderProperty recorderProperty;

    private String wavFilePath;

    public LingoRecorder() {
        this.recorderProperty = new RecorderProperty();
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    /**
     *
     * @deprecated use {@link #isProcessing()} instead
     */
    @Deprecated
    public boolean isAvailable() {
        return available;
    }

    public boolean isRecording() {
        return internalRecorder != null;
    }

    public boolean start() {
        return start(null);
    }

    public boolean start(String outputFilePath) {
        if (internalRecorder != null || isProcessing) {
            if (internalRecorder != null) {
                LOG.e("start fail recorder is recording");
            } else {
                LOG.e("start fail recorder is processing");
            }
            return false;
        }
        LOG.d("start record");
        IRecorder recorder = null;
        if (wavFilePath != null) {
            recorder = new WavFileRecorder(wavFilePath, recorderProperty);
            // wavFileRecorder not support stop
            // LingoRecorder will be available until process finish
            available = false;
        } else {
            recorder = new AndroidRecorder(recorderProperty);
        }

        // clone audioProcessorMap and skip null processor
        Map<String, AudioProcessor> immutableMap = new HashMap<>(audioProcessorMap.size());
        for (String key : audioProcessorMap.keySet()) {
            AudioProcessor audioProcessor = audioProcessorMap.get(key);
            if (audioProcessor != null) {
                immutableMap.put(key, audioProcessor);
            }
        }
        internalRecorder = new InternalRecorder(recorder, outputFilePath, immutableMap.values(),
                new RecorderHandler(this, immutableMap),
                volumeCalculator);
        isProcessing = true;
        internalRecorder.start();
        return true;
    }

    public void stop() {
        if (internalRecorder != null) {
            LOG.d("end record");
            available = false;
            LOG.d("record unavailable now");
            internalRecorder.stop();
            internalRecorder = null;
        }
    }

    public void cancel() {
        if (internalRecorder != null) {
            available = false;
            internalRecorder.cancel();
            internalRecorder = null;
        }
    }

    public void setOnRecordStopListener(OnRecordStopListener onRecordStopListener) {
        this.onRecordStopListener = onRecordStopListener;
    }

    public void setOnProcessStopListener(OnProcessStopListener onProcessStopListener) {
        this.onProcessStopListener = onProcessStopListener;
    }

    public void setOnVolumeListener(OnVolumeListener onVolumeListener) {
        setOnVolumeListener(onVolumeListener, new DefaultVolumeCalculator());
    }

    public void setOnVolumeListener(OnVolumeListener onVolumeListener, IVolumeCalculator volumeCalculator) {
        this.onVolumeListener = onVolumeListener;
        this.volumeCalculator = volumeCalculator;
    }

    public void put(String processorId, AudioProcessor processor) {
        audioProcessorMap.put(processorId, processor);
    }

    public AudioProcessor remove(String processorId) {
        return audioProcessorMap.remove(processorId);
    }

    public interface OnRecordStopListener {

        class Result {
            private long durationInMills;
            private String outputFilePath;

            public long getDurationInMills() {
                return durationInMills;
            }

            public String getOutputFilePath() {
                return outputFilePath;
            }
        }

        void onRecordStop(Throwable throwable, Result result);
    }

    public interface OnProcessStopListener {
        void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map);
    }

    public LingoRecorder sampleRate(int sampleRate) {
        recorderProperty.setSampleRate(sampleRate);
        return this;
    }

    public LingoRecorder channels(int channels) {
        recorderProperty.setChannels(channels);
        return this;
    }

    public LingoRecorder bitsPerSample(int bitsPerSample) {
        recorderProperty.setBitsPerSample(bitsPerSample);
        return this;
    }

    public RecorderProperty getRecorderProperty() {
        return recorderProperty;
    }

    public LingoRecorder wavFile(String filePath) {
        this.wavFilePath = filePath;
        return this;
    }

    private static class InternalRecorder implements Runnable {

        private volatile boolean shouldRun;
        private volatile boolean cancel;
        private volatile Throwable processorsError;

        private Thread thread;
        private IRecorder recorder;
        private Collection<AudioProcessor> audioProcessors;
        private Handler handler;
        private String outputFilePath;
        private IVolumeCalculator volumeCalculator;

        InternalRecorder(
                IRecorder recorder,
                String outputFilePath,
                Collection<AudioProcessor> audioProcessors,
                Handler handler,
                IVolumeCalculator volumeCalculator) {
            thread = new Thread(this);
            this.audioProcessors = audioProcessors;
            this.handler = handler;
            this.recorder = recorder;
            this.outputFilePath = outputFilePath;
            this.volumeCalculator = volumeCalculator;
        }

        void cancel() {
            shouldRun = false;
            cancel = true;
        }

        void stop() {
            shouldRun = false;
        }

        void start() {
            thread.start();
        }

        @Override
        public void run() {
            shouldRun = true;

            WavProcessor wavProcessor = null;
            Throwable recordException = null;
            ProcessThread processThread = null;

            try {
                int buffSize = recorder.getBufferSize();
                byte[] bytes = new byte[buffSize];

                processThread = new ProcessThread();
                processThread.start();

                recorder.startRecording();

                if (outputFilePath != null) {
                    wavProcessor = new WavProcessor(outputFilePath, recorder.getRecordProperty());
                    wavProcessor.start();
                }
                while (shouldRun) {
                    int result = recorder.read(bytes, buffSize);
                    LOG.d("read buffer result = " + result);
                    if (result > 0) {
                        if (volumeCalculator != null) {
                            final long startCalculateTime = System.currentTimeMillis();
                            final double volume = volumeCalculator.onAudioChunk(bytes,
                                    result, recorder.getRecordProperty().getBitsPerSample());
                            final long calculateDuration = System.currentTimeMillis() - startCalculateTime;
                            LOG.d("duration of calculating chunk volume: " + calculateDuration);
                            handler.sendMessage(handler.obtainMessage(MESSAGE_VOLUME, volume));
                        }

                        processThread.process(bytes, result);

                        if (wavProcessor != null) {
                            wavProcessor.flow(bytes, result);
                        }
                    } else if (result < 0) {
                        LOG.d("exit read from recorder result = " + result);
                        shouldRun = false;
                        break;
                    }
                }
                if (wavProcessor != null) {
                    wavProcessor.end();
                }
            } catch (Throwable e) {
                LOG.e(e);
                recordException = e;
            } finally {
                shouldRun = false;

                if (wavProcessor != null) {
                    wavProcessor.release();
                }

                recorder.release();

                // notify recorder stop
                Message message = Message.obtain();
                message.what = MESSAGE_RECORD_STOP;
                Bundle bundle = new Bundle();
                bundle.putLong(KEY_DURATION, recorder.getDurationInMills());
                bundle.putString(KEY_FILEPATH, outputFilePath);
                message.setData(bundle);
                message.obj = recordException;
                handler.sendMessage(message);

                if (recordException != null) {
                    cancel = true;
                }

                // try to end processor thread
                if (processThread != null) {
                    processThread.end(cancel);
                }

                // notify processor stop
                Message msg = Message.obtain();
                msg.what = MESSAGE_PROCESS_STOP;
                if (recordException != null) {
                    msg.obj = new RecordErrorCancelProcessingException(processorsError);
                } else {
                    msg.obj = processorsError;
                }
                handler.sendMessage(msg);
            }
        }

        class ProcessThread extends Thread {

            private LinkedBlockingQueue<Object> processorQueue;

            ProcessThread() {
                this(new LinkedBlockingQueue<>());
            }

            ProcessThread(final LinkedBlockingQueue<Object> processorQueue) {
                super("processThread");
                this.processorQueue = processorQueue;
            }

            void process(@NonNull byte[] bytes, int buffSize) throws InterruptedException {
                WrapBuffer wrapBuffer = new WrapBuffer();
                wrapBuffer.setBytes(Arrays.copyOf(bytes, bytes.length));
                wrapBuffer.setSize(buffSize);
                processorQueue.put(wrapBuffer);
            }

            void end(boolean cancel) {
                try {
                    processorQueue.put("end");
                    if (cancel) {
                        interrupt();
                    }
                    join();
                    LOG.d("processorThread end");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void run() {
                super.run();
                Object value;
                try {
                    for (AudioProcessor audioProcessor : audioProcessors) {
                        checkIfNeedCancel();
                        audioProcessor.start();
                    }
                    while ((value = processorQueue.take()) != null) {
                        if (value instanceof WrapBuffer) {
                            for (AudioProcessor audioProcessor : audioProcessors) {
                                checkIfNeedCancel();
                                WrapBuffer wrapBuffer = (WrapBuffer) value;
                                audioProcessor.flow(wrapBuffer.getBytes(), wrapBuffer.getSize());
                            }

                            for (AudioProcessor audioProcessor : audioProcessors) {
                                checkIfNeedCancel();
                                if (audioProcessor.needExit()) {
                                    LOG.d(String.format("exit because %s", audioProcessor));
                                    shouldRun = false;
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    for (AudioProcessor audioProcessor : audioProcessors) {
                        checkIfNeedCancel();
                        audioProcessor.end();
                    }
                } catch (InterruptedException e) {
                    processorsError = new CancelProcessingException(e);
                } catch (Throwable e) {
                    processorsError = e;
                    LOG.e(e);
                } finally {
                    shouldRun = false;
                    for (AudioProcessor audioProcessor : audioProcessors) {
                        audioProcessor.release();
                    }
                }
            }
        }

        private void checkIfNeedCancel() {
            if (cancel) {
                throw new CancelProcessingException();
            }
        }
    }

    private static class RecorderHandler extends Handler {

        private LingoRecorder mLingoRecorder;
        private Map<String, AudioProcessor> mAudioProcessorMap;

        RecorderHandler(LingoRecorder lingoRecorder, Map<String, AudioProcessor> audioProcessorMap) {
            super(Looper.getMainLooper());
            mLingoRecorder = lingoRecorder;
            mAudioProcessorMap = audioProcessorMap;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MESSAGE_RECORD_STOP:
                    mLingoRecorder.internalRecorder = null;
                    handleRecordStop(msg);
                    break;
                case MESSAGE_PROCESS_STOP:
                    mLingoRecorder.available = true;
                    mLingoRecorder.isProcessing = false;
                    handleProcessStop(msg);
                    break;
                case MESSAGE_VOLUME:
                    if (mLingoRecorder.onVolumeListener != null) {
                        mLingoRecorder.onVolumeListener.onVolume((Double) msg.obj);
                    }
                    break;
            }
        }

        private void handleRecordStop(Message msg) {
            long durationInMills = msg.getData().getLong(KEY_DURATION, -1);
            String outputFilePath = msg.getData().getString(KEY_FILEPATH);
            if (mLingoRecorder.onRecordStopListener != null) {
                OnRecordStopListener.Result result = new OnRecordStopListener.Result();
                result.durationInMills = durationInMills;
                result.outputFilePath = outputFilePath;
                mLingoRecorder.onRecordStopListener.onRecordStop((Throwable) msg.obj, result);
            }
            LOG.d("record end");
        }

        private void handleProcessStop(Message msg) {
            if (mLingoRecorder.onProcessStopListener != null) {
                mLingoRecorder.onProcessStopListener.onProcessStop((Throwable) msg.obj, mAudioProcessorMap);
            }
            LOG.d("process end");
        }
    }

    public static class CancelProcessingException extends RuntimeException {

        public CancelProcessingException() {
            super("cancel processing");
        }

        public CancelProcessingException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class RecordErrorCancelProcessingException extends CancelProcessingException {

        public RecordErrorCancelProcessingException(Throwable throwable) {
            super(throwable);
        }
    }

    public void setDebugEnable(boolean enable) {
        LOG.isEnable = enable;
    }
}
