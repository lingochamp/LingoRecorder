package com.liulishuo.engzo.lingorecorder.demo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.liulishuo.engzo.IAudioProcessorService;
import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.utils.LOG;

import java.util.concurrent.CountDownLatch;

/**
 * Created by wcw on 4/14/17.
 */

public class LocalScorerProcessor implements AudioProcessor {

    private boolean mBound = false;
    private IAudioProcessorService scorerService;
    private CountDownLatch countDownLatch;
    private boolean hasRelease = false;
    private Application application = null;
    private String spokenText;
    private int score;

    public LocalScorerProcessor(Application  application, String spokenText) {
        this.spokenText = spokenText;
        this.application = application;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBound = true;
            scorerService = IAudioProcessorService.Stub.asInterface(service);
            countDownLatch.countDown();


            // avoid ActivityManager: Scheduling restart of crashed service
            if (hasRelease) {
                application.unbindService(this);
                mBound = false;
            }
            LOG.d("localScorer onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            scorerService = null;
            LOG.d("localScorer onServiceDisconnected");
        }
    };

    @Override
    public void start() throws Exception {
        countDownLatch = new CountDownLatch(1);
        Intent intent = new Intent(application, ScorerService.class);
        long startTime = System.currentTimeMillis();
        application.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        countDownLatch.await();
        LOG.d(String.format("localScorer cost %dms to connect service",
                System.currentTimeMillis() - startTime));

        Bundle bundle = new Bundle();
        bundle.putString("spokenText", spokenText);

        scorerService.init(bundle);
        scorerService.start();
    }

    @Override
    public void flow(byte[] bytes, int size) throws Exception {
        scorerService.flow(bytes, size);
    }

    @Override
    public boolean needExit() {
        try {
            return scorerService.needExit();
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void end() throws Exception {
        scorerService.end();
        score = scorerService.getResult().getInt("score");
    }

    @Override
    public void release() {
        try {
            hasRelease = true;
            if (mBound) {
                application.unbindService(mConnection);
                mBound = false;
            }
        } catch (Exception ex) {
            LOG.e(ex);
        }
    }

    public int getScore() {
        return score;
    }
}
