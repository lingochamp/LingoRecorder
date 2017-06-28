package com.liulishuo.engzo.lingorecorder.processor;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by wcw on 3/28/17.
 */

public class WavProcessor implements AudioProcessor {

    private String filePath;
    private RandomAccessFile writer;
    private int payloadSize = 0;

    public WavProcessor(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void start() throws Exception {
        payloadSize = 0;
        short nChannels = 1;
        int sRate = 16000;
        short mBitsPersample = 16;

        // http://soundfile.sapp.org/doc/WaveFormat/
        writer = new RandomAccessFile(filePath, "rw");
        writer.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
        writer.writeBytes("RIFF");
        writer.writeInt(0); // Final file size not known yet, write 0
        writer.writeBytes("WAVE");
        writer.writeBytes("fmt ");
        writer.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
        writer.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
        writer.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
        writer.writeInt(Integer.reverseBytes(sRate)); // Sample rate
        writer.writeInt(Integer.reverseBytes(sRate*nChannels*mBitsPersample/8)); // Byte rate, SampleRate*NumberOfChannels*mBitsPersample/8
        writer.writeShort(Short.reverseBytes((short)(nChannels*mBitsPersample/8))); // Block align, NumberOfChannels*mBitsPersample/8
        writer.writeShort(Short.reverseBytes(mBitsPersample)); // Bits per sample
        writer.writeBytes("data");
        writer.writeInt(0); // Data chunk size not known yet, write 0

    }

    @Override
    public void flow(byte[] bytes, int result) throws Exception {
        if (result > 0)  {
            writer.write(bytes);
            payloadSize += result;
        }
    }

    @Override
    public boolean needExit() {
        return false;
    }

    @Override
    public void end() throws Exception {
        writer.seek(4); // Write size to RIFF header
        writer.writeInt(Integer.reverseBytes(36 + payloadSize));

        writer.seek(40); // Write size to Subchunk2Size field
        writer.writeInt(Integer.reverseBytes(payloadSize));
    }

    @Override
    public void release() {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
