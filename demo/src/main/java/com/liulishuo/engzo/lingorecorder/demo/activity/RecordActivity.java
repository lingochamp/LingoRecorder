package com.liulishuo.engzo.lingorecorder.demo.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.liulishuo.engzo.lingorecorder.LingoRecorder;
import com.liulishuo.engzo.lingorecorder.demo.R;
import com.liulishuo.engzo.lingorecorder.demo.RecordPermissionHelper;
import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;

import java.util.Map;

/**
 * Created by rantianhua on 17/8/29.
 */

public abstract class RecordActivity extends AppCompatActivity {

    private static final String TAG = "LingoRecorder";

    protected RecordPermissionHelper recordPermissionHelper;
    protected LingoRecorder lingoRecorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordPermissionHelper = new RecordPermissionHelper(this);
        recordPermissionHelper.setGrantedListener(
                new RecordPermissionHelper.PermissionGrantedListener() {
                    @Override
                    public void onPermissionGranted() {
                        RecordActivity.this.onPermissionGranted();
                    }
                });
        lingoRecorder = new LingoRecorder();
        lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {
            @Override
            public void onRecordStop(Throwable throwable,
                    Result result) {
                if (throwable != null) {
                    RecordActivity.this.onRecordError(throwable);
                } else {
                    RecordActivity.this.onRecordStop(result);
                }
            }
        });
        lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
            @Override
            public void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map) {
                if (throwable != null) {
                    RecordActivity.this.onProcessError(throwable);
                } else {
                    RecordActivity.this.onProcessStop(map);
                }
            }
        });
    }

    protected void onProcessError(Throwable throwable) {
        Toast.makeText(this, getString(R.string.process_failed), Toast.LENGTH_SHORT).show();
        Log.e(TAG, Log.getStackTraceString(throwable), throwable);
    }

    protected void onRecordError(Throwable throwable) {
        Toast.makeText(this, getString(R.string.record_failed), Toast.LENGTH_SHORT).show();
        Log.e(TAG, Log.getStackTraceString(throwable), throwable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        recordPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected boolean checkRecordPermission() {
        return recordPermissionHelper.checkRecordPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lingoRecorder.isRecording()) {
            lingoRecorder.cancel();
        }
    }

    protected abstract void onProcessStop(Map<String, AudioProcessor> map);

    protected abstract void onRecordStop(LingoRecorder.OnRecordStopListener.Result result);

    protected abstract void onPermissionGranted();
}
