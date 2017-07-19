package com.liulishuo.engzo.lingorecorder.processor;

import java.io.RandomAccessFile;

/**
 * Created by wcw on 3/28/17.
 */

public class WavProcessor implements AudioProcessor {

    private String filePath;
    private RandomAccessFile writer;
    private int payloadSize = 0;

    private short nChannels = 1;
    private int sampleRate = 16000;
    private short bitsPerSample = 16;

    public WavProcessor(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void start() throws Exception {
        payloadSize = 0;
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
        writer.writeInt(Integer.reverseBytes(sampleRate)); // Sample rate
        writer.writeInt(Integer.reverseBytes(sampleRate *nChannels* bitsPerSample /8)); // Byte rate, SampleRate*NumberOfChannels*bitsPerSample/8
        writer.writeShort(Short.reverseBytes((short)(nChannels* bitsPerSample /8))); // Block align, NumberOfChannels*bitsPerSample/8
        writer.writeShort(Short.reverseBytes(bitsPerSample)); // Bits per sample
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

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getDurationInMills() {
        return (long) (payloadSize * 8.0 * 1000 / bitsPerSample / sampleRate / nChannels );
    }
}
