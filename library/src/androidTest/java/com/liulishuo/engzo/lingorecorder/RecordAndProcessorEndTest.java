package com.liulishuo.engzo.lingorecorder;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by rantianhua on 17/7/31.
 * this class will test the end callback of record and processor
 * will be invoked at a fixed order. (recorder end callback always first)
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RecordAndProcessorEndTest {

    private AudioProcessor testProcessor;
    private LingoRecorder lingoRecorder;
    private long recorderEnd;
    private long processorEnd;

    @Before
    public void before() {
        lingoRecorder = new LingoRecorder();
    }

    @Test
    public void recorderEndFirstRecorderCallbackFirst() throws Exception {
        testProcessor = new AudioProcessor() {
            @Override
            public void start() throws Exception {

            }

            @Override
            public void flow(byte[] bytes, int size) throws Exception {

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
        };
        lingoRecorder.put("test1", testProcessor);

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {

            @Override
            public void onRecordStop(Throwable throwable,
                    Result result) {
                recorderEnd = System.nanoTime();
                countDownLatch.countDown();
            }

        });
        lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
            @Override
            public void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map) {
                processorEnd = System.nanoTime();
                countDownLatch.countDown();
            }
        });

        lingoRecorder.start();
        sleep();
        lingoRecorder.stop();

        countDownLatch.await();

        Assert.assertTrue("OnProcessStopListener must callback after OnRecordStopListener",
                processorEnd > recorderEnd);
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
    }

    private String processorErrorMsg = null;
    @Test
    public void processorMayEndFirstRecorderCallbackFirst() throws Exception {
        final String errorMsg = "hah";
        testProcessor = new AudioProcessor() {
            @Override
            public void start() throws Exception {
                throw new RuntimeException(errorMsg);
            }

            @Override
            public void flow(byte[] bytes, int size) throws Exception {

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
        };
        lingoRecorder.put("test2", testProcessor);

        final CountDownLatch countDownLatch = new CountDownLatch(2);


        lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {

            @Override
            public void onRecordStop(Throwable throwable,
                    Result result) {
                recorderEnd = System.nanoTime();
                countDownLatch.countDown();
            }
        });
        lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
            @Override
            public void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map) {
                processorErrorMsg = throwable.getMessage();
                processorEnd = System.nanoTime();
                countDownLatch.countDown();
            }
        });

        lingoRecorder.start();
        sleep();
        lingoRecorder.stop();

        countDownLatch.await();

        Assert.assertTrue("OnProcessStopListener must callback after OnRecordStopListener",
                processorEnd > recorderEnd);
        Assert.assertThat("processor exception should be obtained by OnProcessStopListener",
                processorErrorMsg,
                CoreMatchers.is(errorMsg));
    }

}
