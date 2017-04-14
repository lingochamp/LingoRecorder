package com.liulishuo.engzo.lingorecorder.demo;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.liulishuo.engzo.IAudioProcessorService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Random;

/**
 * Created by wcw on 4/10/17.
 */

public class ScorerService extends Service {

    private final IAudioProcessorService.Stub mBinder = new IAudioProcessorService.Stub() {

        private Random random;
        private Bundle resultBundle;

        @Override
        public void init(Bundle bundle) throws RemoteException {
            resultBundle = new Bundle();
        }

        @Override
        public void start() throws RemoteException {
            random = new Random();
        }

        @Override
        public void flow(byte[] bytes, int result) throws RemoteException {

        }

        @Override
        public boolean needExit() throws RemoteException {
            return false;
        }

        @Override
        public void end() throws RemoteException {
            resultBundle.putInt("score", random.nextInt(100));
        }

        @Override
        public void release() throws RemoteException {
            random = null;
        }

        @Override
        public Bundle getResult() throws RemoteException {
            return resultBundle;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


}
