package com.liulishuo.engzo;

interface IAudioProcessorService {

    void init(in Bundle bundle);

    void start();
    void flow(in byte[] bytes, int result);
    boolean needExit();
    void end();
    void release();

    Bundle getResult();

}

