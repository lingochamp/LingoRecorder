package com.liulishuo.engzo.lingorecorder.demo.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.liulishuo.engzo.lingorecorder.LingoRecorder;
import com.liulishuo.engzo.lingorecorder.demo.R;
import com.liulishuo.engzo.lingorecorder.demo.view.VolumeView;
import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.volume.OnVolumeListener;

import java.util.Map;

/**
 * Created by rantianhua on 2017/9/26.
 * demonstrate the volume value during recording
 */

public class VolumeDemonstrateActivity extends RecordActivity {

    private Button btnRecord;
    private VolumeView volumeView;
    private double maxVolume = 90;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume);

        volumeView = (VolumeView) findViewById(R.id.volume_view);
        btnRecord = (Button) findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkRecordPermission()) return;
                handleRecorderBtn();
            }
        });

        lingoRecorder.setOnVolumeListener(new OnVolumeListener() {
            @Override
            public void onVolume(double volume) {
                double rate = volume / maxVolume;
                //more sensitive to volume change
                Log.d("VolumeDemonstrate", "rate is " + rate);
                rate = rate * rate * rate;
                volumeView.setAmplitude(rate);
            }
        });

        lingoRecorder.setDebugEnable(true);
    }

    private void handleRecorderBtn() {
        if (lingoRecorder.isRecording()) {
            lingoRecorder.stop();
        } else if (!lingoRecorder.isProcessing()){
            lingoRecorder.start();
            volumeView.startWave();
            btnRecord.setText(R.string.stop_record);
        }
    }

    @Override
    protected void onRecordError(Throwable throwable) {
        super.onRecordError(throwable);
        btnRecord.setText(R.string.start_record);
    }

    @Override
    protected void onProcessError(Throwable throwable) {
        super.onProcessError(throwable);
        btnRecord.setText(R.string.start_record);
    }

    @Override
    protected void onProcessStop(Map<String, AudioProcessor> map) {
        btnRecord.setText(R.string.start_record);
    }

    @Override
    protected void onRecordStop(LingoRecorder.OnRecordStopListener.Result result) {
        volumeView.stopWave();
    }

    @Override
    protected void onPermissionGranted() {
        btnRecord.performClick();
    }
}
