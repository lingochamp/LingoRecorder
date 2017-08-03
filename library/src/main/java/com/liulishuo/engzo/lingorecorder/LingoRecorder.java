package com.liulishuo.engzo.lingorecorder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.recorder.AndroidRecorder;
import com.liulishuo.engzo.lingorecorder.recorder.IRecorder;
import com.liulishuo.engzo.lingorecorder.recorder.WavFileRecorder;
import com.liulishuo.engzo.lingorecorder.utils.LOG;
import com.liulishuo.engzo.lingorecorder.utils.WrapBuffer;

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
    private final static String KEY_DURATION = "duration";

    private OnRecordStopListener onRecordStopListener;
    private OnProcessStopListener onProcessStopListener;

    private boolean available = true;

    private Handler handler = new Handler(Looper.getMainLooper()) {

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
            }
        }

        private void handleRecordStop(Message msg) {
            long durationInMills = msg.getData().getLong(KEY_DURATION, -1);
            if (onRecordStopListener != null) {
                OnRecordStopListener.Result result = new OnRecordStopListener.Result();
                result.durationInMills = durationInMills;
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

    public boolean isAvailable() {
        return available;
    }

    public boolean isRecording() {
        return internalRecorder != null;
    }

    public void start() {
        LOG.d("start record");
        IRecorder recorder = null;
        if (wavFilePath != null) {
            recorder = new WavFileRecorder(wavFilePath);
            // wavFileRecorder not support stop
            // LingoRecorder will be available until process finish
            available = false;
        } else {
            recorder = new AndroidRecorder(sampleRate, channels, bitsPerSample);
        }
        internalRecorder = new InternalRecorder(recorder, audioProcessorMap.values(), handler);
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

    public void setOnRecordStopListener(OnRecordStopListener onRecordStopListener) {
        this.onRecordStopListener = onRecordStopListener;
    }

    public void setOnProcessStopListener(OnProcessStopListener onProcessStopListener) {
        this.onProcessStopListener = onProcessStopListener;
    }

    public void put(String processorId, AudioProcessor processor) {
        audioProcessorMap.put(processorId, processor);
    }

    public interface OnRecordStopListener {

        class Result {
            private long durationInMills;

            public long getDurationInMills() {
                return durationInMills;
            }
        }

        void onRecordStop(Throwable throwable, Result result);
    }

    public interface OnProcessStopListener {
        void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map);
    }

    private int sampleRate = 16000;
    private int channels = 1;
    private int bitsPerSample = 16;
    private String wavFilePath;

    public LingoRecorder sampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public LingoRecorder channels(int channels) {
        this.channels = channels;
        return this;
    }

    public LingoRecorder bitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
        return this;
    }

    public LingoRecorder wavFile(String filePath) {
        this.wavFilePath = filePath;
        return this;
    }

    @Deprecated
    public LingoRecorder testFile(String filePath) {
        this.wavFilePath = filePath;
        return this;
    }

    private static class InternalRecorder implements Runnable {

        private volatile boolean shouldRun;
        private volatile Throwable processorsError;

        private Thread thread;
        private IRecorder recorder;
        private Collection<AudioProcessor> audioProcessors;
        private Handler handler;

        InternalRecorder(IRecorder recorder, Collection<AudioProcessor> audioProcessors, Handler handler) {
            thread = new Thread(this);
            this.audioProcessors = audioProcessors;
            this.handler = handler;
            this.recorder = recorder;
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

            final LinkedBlockingQueue<Object> processorQueue = new LinkedBlockingQueue<>();
            Thread processorThread = new Thread("process audio data") {

                @Override
                public void run() {
                    super.run();
                    Object value;
                    try {
                        for (AudioProcessor audioProcessor : audioProcessors) {
                            audioProcessor.start();
                        }
                        while ((value = processorQueue.take()) != null) {
                            if (value instanceof WrapBuffer) {
                                for (AudioProcessor audioProcessor : audioProcessors) {
                                    WrapBuffer wrapBuffer = (WrapBuffer) value;
                                    audioProcessor.flow(wrapBuffer.getBytes(), wrapBuffer.getSize());
                                }

                                boolean shouldBreak = false;
                                for (AudioProcessor audioProcessor : audioProcessors) {
                                    if (audioProcessor.needExit()) {
                                        LOG.d(String.format("exit because %s", audioProcessor));
                                        shouldBreak = true;
                                        break;
                                    }
                                }
                                if (shouldBreak) break;
                            } else {
                                break;
                            }
                        }
                        for (AudioProcessor audioProcessor : audioProcessors) {
                            audioProcessor.end();
                        }
                    } catch (Throwable e) {
                        processorsError = e;
                        LOG.e(e);
                    } finally {
                        for (AudioProcessor audioProcessor : audioProcessors) {
                            audioProcessor.release();
                        }
                        shouldRun = false;
                    }
                }
            };
            processorThread.start();

            try {
                recorder.startRecording();
                while (shouldRun) {
                    int result = recorder.read(bytes, buffSize);
                    LOG.d("read buffer result = " + result);
                    if (result > 0) {
                        WrapBuffer wrapBuffer = new WrapBuffer();
                        wrapBuffer.setBytes(Arrays.copyOf(bytes, bytes.length));
                        wrapBuffer.setSize(result);
                        processorQueue.put(wrapBuffer);
                    } else if (result < 0) {
                        LOG.d("exit read from recorder result = " + result);
                        shouldRun = false;
                        break;
                    }
                }
            } catch (Throwable e) {
                LOG.e(e);
            } finally {
                shouldRun = false;

                //notify stop record
                Message message = Message.obtain();
                message.what = MESSAGE_RECORD_STOP;
                Bundle bundle = new Bundle();
                bundle.putLong(KEY_DURATION, recorder.getDurationInMills());
                message.setData(bundle);
                handler.sendMessage(message);

                //ensure processors' tread has been end
                try {
                    processorQueue.put("end");
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
    };



}
