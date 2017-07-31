package com.liulishuo.engzo.lingorecorder.demo;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.utils.LOG;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by wcw on 3/30/17.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class AndroidFlacProcessor implements AudioProcessor {

    private String filePath;
    private MediaCodec codec;

    private FileOutputStream fos;

    public AndroidFlacProcessor(String filePath) {
        this.filePath = filePath;
    }


    @Override
    public void start() {
        try {
            fos = new FileOutputStream(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaFormat format  = new MediaFormat();
        String mime = "audio/flac";
        format.setString(MediaFormat.KEY_MIME, mime);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 16000);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

        String componentName = getEncoderNamesForType(mime);
        try {
            codec = MediaCodec.createByCodecName(componentName);
            codec.configure(
                    format,
                    null /* surface */,
                    null /* crypto */,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void flow(byte[] bytes, int size) {
        try {
            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            int inputBufferIndex = codec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(bytes);
                codec.queueInputBuffer(inputBufferIndex, 0, size, 0, 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);

            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                fos.write(outData, 0, outData.length);
                LOG.d("FlacEncoder " + outData.length + " bytes written");

                codec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);

            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public boolean needExit() {
        return false;
    }

    @Override
    public void end() {
        try {
            if (codec != null) {
                codec.stop();
                codec.release();
                codec = null;
            }

            if (fos != null) {
                fos.flush();
                fos.close();
                fos = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public void release() {
        end();
    }

    private String getEncoderNamesForType(String mime) {
        int n = MediaCodecList.getCodecCount();
        String name = null;
        for (int i = 0; i < n; ++i) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }
            if (!info.getName().startsWith("OMX.")) {
                // Unfortunately for legacy reasons, "AACEncoder", a
                // non OMX component had to be in this list for the video
                // editor code to work... but it cannot actually be instantiated
                // using MediaCodec.
                continue;
            }
            String[] supportedTypes = info.getSupportedTypes();
            for (int j = 0; j < supportedTypes.length; ++j) {
                if (supportedTypes[j].equalsIgnoreCase(mime)) {
                    name = info.getName();
                    return name;
                }
            }
        }
        return name;
    }
}
