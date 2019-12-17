package com.liulishuo.engzo.lingorecorder;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.processor.TimerProcessor;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by wcw on 8/9/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LingoRecorderTest {

    private LingoRecorder lingoRecorder;

    @Before
    public void before() {
        lingoRecorder = new LingoRecorder();
    }

    @Test
    public void testRecorderStopWhenProcessorThrowException() {
        lingoRecorder.put("exception", new AudioProcessor() {
            @Override
            public void start() throws Exception {

            }

            @Override
            public void flow(byte[] bytes, int size) throws Exception {
                throw new RuntimeException("exception");
            }

            @Override
            public boolean needExit() {
                return false;
            }

            @Override
            public void end() throws Exception {

            }

            @Override
            public void release() {

            }
        });
        lingoRecorder.put("timer", new TimerProcessor(lingoRecorder.getRecorderProperty(), 1000));

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final boolean[] status = new boolean[1];

        lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {
            @Override
            public void onRecordStop(Throwable throwable, Result result) {
                countDownLatch.countDown();
                status[0] = true;
            }
        });

        lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
            @Override
            public void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map) {
                countDownLatch.countDown();
            }
        });

        lingoRecorder.start();

        try {
            countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(status[0]);


    }

    private long getFileSize(String file) {
        return new File(file).length();
    }
}
