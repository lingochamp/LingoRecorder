package com.liulishuo.engzo.lingorecorder;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wcw on 8/9/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CancelRecordTest {

    private LingoRecorder lingoRecorder;

    @Before
    public void before() {
        lingoRecorder = new LingoRecorder();
    }

    @Test
    public void testCancelRecorderWhenProcessingBlock() {
        lingoRecorder.put("blockProcessing", new AudioProcessor() {

            private void block(long time) throws InterruptedException {
                Thread.sleep(time);
            }

            @Override
            public void start() throws Exception {
                block(100000);
            }

            @Override
            public void flow(byte[] bytes, int size) throws Exception {
                block(100000);
            }

            @Override
            public boolean needExit() {
                return false;
            }

            @Override
            public void end() throws Exception {
                block(100000);
            }

            @Override
            public void release() {

            }
        });

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final Throwable[] throwables = new Throwable[2];

        lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
            @Override
            public void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map) {
                throwables[1] = throwable;
                countDownLatch.countDown();
            }
        });

        lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {
            @Override
            public void onRecordStop(Throwable throwable, Result result) {
                throwables[0] = throwable;
                countDownLatch.countDown();
            }
        });

        lingoRecorder.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lingoRecorder.cancel();

        long startTime = System.currentTimeMillis();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long cancelCostTime = System.currentTimeMillis() - startTime;

        Assert.assertTrue(cancelCostTime < 1000);

        Assert.assertNull(throwables[0]);
        Assert.assertNotNull(throwables[1]);
        Log.e(CancelRecordTest.class.getSimpleName(), Log.getStackTraceString(throwables[1]));
        Assert.assertEquals(LingoRecorder.CancelProcessingException.class, throwables[1].getClass());


    }

}
