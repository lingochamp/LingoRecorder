package com.liulishuo.engzo.lingorecorder.processor;

public class ProcessorProfilerWrapper implements AudioProcessor {

    public static Profiler profiler;
    private AudioProcessor processor;
    private String name;

    public ProcessorProfilerWrapper(String name, AudioProcessor processor) {
        this.processor = processor;
        this.name = name;
    }

    @Override
    public void start() throws Exception {
        long startMethodStartTimeStamp = System.currentTimeMillis();
        processor.start();
        if (profiler != null) {
            profiler.start(
                    name,
                    System.currentTimeMillis() - startMethodStartTimeStamp
            );
        }
    }

    @Override
    public void flow(byte[] bytes, int size) throws Exception {
        long flowMethodStartTimeStamp = System.currentTimeMillis();
        processor.flow(bytes, size);
        if (profiler != null) {
            profiler.flow(
                    name,
                    System.currentTimeMillis() - flowMethodStartTimeStamp
            );
        }
    }

    @Override
    public boolean needExit() {
        return processor.needExit();
    }

    @Override
    public void end() throws Exception {
        long endMethodStartTimeStamp = System.currentTimeMillis();
        processor.end();
        if (profiler != null) {
            profiler.end(
                    name,
                    System.currentTimeMillis() - endMethodStartTimeStamp
            );
        }
    }

    @Override
    public void release() {
        long releaseMethodStartTimeStamp = System.currentTimeMillis();
        processor.release();
        if (profiler != null) {
            profiler.release(
                    name,
                    System.currentTimeMillis() - releaseMethodStartTimeStamp
            );
        }
    }

    public interface Profiler {

        void start(String name, long cost);

        void flow(String name, long cost);

        void end(String name, long cost);

        void release(String name, long cost);
    }
}
