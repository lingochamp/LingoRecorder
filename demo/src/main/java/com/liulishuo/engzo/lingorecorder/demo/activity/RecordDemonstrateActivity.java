package com.liulishuo.engzo.lingorecorder.demo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.liulishuo.engzo.lingorecorder.LingoRecorder;
import com.liulishuo.engzo.lingorecorder.demo.R;
import com.liulishuo.engzo.lingorecorder.demo.Utils;
import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.volume.OnVolumeListener;

import java.util.Map;

/**
 * Created by rantianhua on 17/8/29.
 * demonstrate how to record with {@link LingoRecorder}
 */

public class RecordDemonstrateActivity extends RecordActivity {

    private Button btnRecord;
    private Button btnPlay;
    private TextView tvRecordDuration;
    private TextView tvRecordSize;
    private EditText etOutputFile;

    private String outputFile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_demonstrate);

        btnRecord = (Button) findViewById(R.id.btn_record);
        btnPlay = (Button) findViewById(R.id.btn_play);
        tvRecordDuration = (TextView) findViewById(R.id.record_duration);
        tvRecordSize = (TextView) findViewById(R.id.record_size);
        etOutputFile = (EditText) findViewById(R.id.et_output_file);

        tvRecordDuration.setText(getString(R.string.record_duration, ""));
        tvRecordSize.setText(getString(R.string.record_size, ""));

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkRecordPermission()) return;
                handleRecorderBtn();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + outputFile), "audio/*");
                startActivity(intent);
            }
        });

        lingoRecorder.setOnVolumeListener(new OnVolumeListener() {
            @Override
            public void onVolume(double volume) {
                Log.d(RecordDemonstrateActivity.class.getSimpleName(), "volume is " + volume);
            }
        });
    }

    private void handleRecorderBtn() {
        if (lingoRecorder.isAvailable()) {
            if (lingoRecorder.isRecording()) {
                lingoRecorder.stop();
            } else {
                lingoRecorder.start(etOutputFile.getText().toString());
                btnRecord.setText(R.string.stop_record);
                btnPlay.setEnabled(false);
            }
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
        btnPlay.setEnabled(true);
        tvRecordSize.setText(getString(R.string.record_size, Utils.formatFileSize(outputFile)));
    }

    @Override
    protected void onRecordStop(LingoRecorder.OnRecordStopListener.Result result) {
        outputFile = result.getOutputFilePath();
        tvRecordDuration.setText(getString(R.string.record_duration,
                Utils.getDurationString(result.getDurationInMills())));
    }

    @Override
    protected void onPermissionGranted() {
        btnRecord.performClick();
    }


}
