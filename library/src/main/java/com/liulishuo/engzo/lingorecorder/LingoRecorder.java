package com.liulishuo.engzo.lingorecorder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
    private final static int MESSAGE_AVAILABLE = 3;
    private final static int MESSAGE_VOLUME = 4;
    private final static String KEY_DURATION = "duration";
    private final static String KEY_FILEPATH = "filePath";

    private OnRecordStopListener onRecordStopListener;
    private OnProcessStopListener onProcessStopListener;
    private OnVolumeListener onVolumeListener;
    private IVolumeCalculator volumeCalculator;

    private boolean available = true;

    private final Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MESSAGE_RECORD_STOP:
                    handleRecordStop(msg);
                    break;
                case MESSAGE_PROCESS_STOP:
                    handleProcessStop(msg);
                    break;
                case MESSAGE_AVAILABLE:
                    available = true;
                    LOG.d("record available now");
                    break;
                case MESSAGE_VOLUME:
                    if (onVolumeListener != null) {
                        onVolumeListener.onVolume((Double) msg.obj);
                    }
                    break;
            }
        }

        private void handleRecordStop(Message msg) {
            long durationInMills = msg.getData().getLong(KEY_DURATION, -1);
            String outputFilePath = msg.getData().getString(KEY_FILEPATH);
            if (onRecordStopListener != null) {
                OnRecordStopListener.Result result = new OnRecordStopListener.Result();
                result.durationInMills = durationInMills;
                result.outputFilePath = outputFilePath;
                onRecordStopListener.onRecordStop((Throwable) msg.obj, result);
            }
            internalRecorder = null;
            LOG.d("record end");
        }

        private void handleProcessStop(Message msg) {
            if (onProcessStopListener != null) {
                onProcessStopListener.onProcessStop((Throwable) msg.obj, audioProcessorMap);
            }
            LOG.d("process end");
        }
    };

    private final RecorderProperty recorderProperty;

    private String wavFilePath;

    public LingoRecorder() {
        this.recorderProperty = new RecorderProperty();
        setVolumeCalculator(new DefaultVolumeCalculator());
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isRecording() {
        return internalRecorder != null;
    }

    public void start() {
        start(null);
    }

    public void start(String outputFilePath) {
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
        internalRecorder = new InternalRecorder(recorder, outputFilePath, audioProcessorMap.values(), handler, onVolumeListener, volumeCalculator);
        internalRecorder.start();
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
        this.onVolumeListener = onVolumeListener;
    }

    public void setVolumeCalculator(IVolumeCalculator volumeCalculator) {
        this.volumeCalculator = volumeCalculator;
    }

    public void put(String processorId, AudioProcessor processor) {
        audioProcessorMap.put(processorId, processor);
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
        private OnVolumeListener onVolumeListener;
        private IVolumeCalculator volumeCalculator;

        InternalRecorder(
                IRecorder recorder,
                String outputFilePath,
                Collection<AudioProcessor> audioProcessors,
                Handler handler,
                OnVolumeListener onVolumeListener,
                IVolumeCalculator volumeCalculator) {
            thread = new Thread(this);
            this.audioProcessors = audioProcessors;
            this.handler = handler;
            this.recorder = recorder;
            this.outputFilePath = outputFilePath;
            this.onVolumeListener = onVolumeListener;
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
            int buffSize = recorder.getBufferSize();

            byte[] bytes = new byte[buffSize];

            LinkedBlockingQueue<Object> processorQueue = new LinkedBlockingQueue<>();
            Thread processorThread = createProcessThread(processorQueue);
            processorThread.start();

            WavProcessor wavProcessor = null;
            if (outputFilePath != null) {
                wavProcessor = new WavProcessor(outputFilePath);
                wavProcessor.setRecordProperty(recorder.getRecordProperty());
            }

            Throwable recordException = null;

            try {
                recorder.startRecording();
                if (wavProcessor != null) {
                    wavProcessor.start();
                }
                while (shouldRun) {
                    int result = recorder.read(bytes, buffSize);
                    LOG.d("read buffer result = " + result);
                    if (result > 0) {
                        WrapBuffer wrapBuffer = new WrapBuffer();
                        wrapBuffer.setBytes(Arrays.copyOf(bytes, bytes.length));
                        wrapBuffer.setSize(result);

                        if (volumeCalculator != null && volumeCalculator != null) {
                            final double volume = volumeCalculator.onAudioChunk(wrapBuffer.getBytes(),
                                    wrapBuffer.getSize(), recorder.getRecordProperty().getBitsPerSample());
                            handler.sendMessage(handler.obtainMessage(MESSAGE_VOLUME, volume));
                        }

                        processorQueue.put(wrapBuffer);
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

                //notify stop record
                Message message = Message.obtain();
                message.what = MESSAGE_RECORD_STOP;
                Bundle bundle = new Bundle();
                bundle.putLong(KEY_DURATION, recorder.getDurationInMills());
                bundle.putString(KEY_FILEPATH, outputFilePath);
                message.setData(bundle);
                message.obj = recordException;
                handler.sendMessage(message);

                //ensure processors' tread has been end
                try {
                    processorQueue.put("end");
                    if (cancel) {
                        processorThread.interrupt();
                    }
                    processorThread.join();
                    LOG.d("processorThread end");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //notify processors end
                Message msg = Message.obtain();
                msg.what = MESSAGE_PROCESS_STOP;
                msg.obj = processorsError;
                handler.sendMessage(msg);

                recorder.release();
                handler.sendEmptyMessage(MESSAGE_AVAILABLE);
            }
        }

        private Thread createProcessThread(final LinkedBlockingQueue<Object> processorQueue) {
            return new Thread("process audio data") {

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
            };
        }

        private void checkIfNeedCancel() {
            if (cancel) {
                throw new CancelProcessingException();
            }
        }
    };

    public static class CancelProcessingException extends RuntimeException {

        public CancelProcessingException() {
            super("cancel processing");
        }

        public CancelProcessingException(Throwable throwable) {
            super(throwable);
        }
    }
}
