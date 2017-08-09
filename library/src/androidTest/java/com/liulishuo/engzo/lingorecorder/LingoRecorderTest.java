package com.liulishuo.engzo.lingorecorder;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.processor.TimerProcessor;
import com.liulishuo.engzo.lingorecorder.processor.WavProcessor;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

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
    public void testStartWithSpecifyOutputFilePath() {
        final String filePathA = "/sdcard/test.wav";
        final String filePathB = "/sdcard/test2.wav";

        new File(filePathA).delete();
        new File(filePathB).delete();

        lingoRecorder.put("wav", new WavProcessor(filePathA));
        lingoRecorder.put("time", new TimerProcessor(1000));

        lingoRecorder.start(filePathB);

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final Long[] fileSizeArray = new Long[2];

        lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {
            @Override
            public void onRecordStop(Throwable throwable, Result result) {
                fileSizeArray[0] = getFileSize(filePathB);
                countDownLatch.countDown();
            }
        });

        lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
            @Override
            public void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map) {
                fileSizeArray[1] = getFileSize(filePathA);
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(fileSizeArray[0] != 0);
        Assert.assertTrue(fileSizeArray[1] != 0);
        Assert.assertEquals(fileSizeArray[0], fileSizeArray[1]);
    }

    private long getFileSize(String file) {
        return new File(file).length();
    }
}
