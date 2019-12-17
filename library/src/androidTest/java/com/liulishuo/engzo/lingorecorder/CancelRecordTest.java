package com.liulishuo.engzo.lingorecorder;

import android.Manifest;
import android.util.Log;

import androidx.test.filters.SmallTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
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

    @Rule
    public GrantPermissionRule pr = GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

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

        final Throwable[] throwableArray = new Throwable[2];

        lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
            @Override
            public void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map) {
                throwableArray[1] = throwable;
                countDownLatch.countDown();
            }
        });

        lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {
            @Override
            public void onRecordStop(Throwable throwable, Result result) {
                throwableArray[0] = throwable;
                countDownLatch.countDown();
            }
        });

        lingoRecorder.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignore) {
        }

        lingoRecorder.cancel();

        long startCancelTime = System.currentTimeMillis();

        try {
            countDownLatch.await();
        } catch (InterruptedException ignore) {
        }

        long cancelCostTime = System.currentTimeMillis() - startCancelTime;

        Assert.assertTrue(cancelCostTime < 1000);

        Assert.assertNull(throwableArray[0]);
        Assert.assertNotNull(throwableArray[1]);
        Log.e(CancelRecordTest.class.getSimpleName(), Log.getStackTraceString(throwableArray[1]));
        Assert.assertEquals(LingoRecorder.CancelProcessingException.class, throwableArray[1].getClass());
    }

}
