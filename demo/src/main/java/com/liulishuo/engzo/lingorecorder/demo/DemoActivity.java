package com.liulishuo.engzo.lingorecorder.demo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.engzo.lingorecorder.LingoRecorder;
import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;
import com.liulishuo.engzo.lingorecorder.processor.TimerProcessor;
import com.liulishuo.engzo.lingorecorder.processor.WavProcessor;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Created by wcw on 4/11/17.
 */

public class DemoActivity extends AppCompatActivity {

    public static final String WAV = "wav";
    public static final String FLAC = "androidFlac";
    public static final String SCORER = "localScorer";
    public static final String TIMER = "timer";

    private static final int REQUEST_CODE_PERMISSION = 10010;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        final TextView resultView = (TextView) findViewById(R.id.resultView);
        TextView titleView = (TextView) findViewById(R.id.titleView);
        final Button recordBtn = (Button) findViewById(R.id.recordBtn);

        String spokenText = "i will study english very hard";

        recordBtn.setText("start");
        titleView.setText("请说 " + spokenText);

        final LingoRecorder lingoRecorder = new LingoRecorder();
        lingoRecorder.put(WAV, new WavProcessor("/sdcard/test.wav"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            lingoRecorder.put(FLAC, new AndroidFlacProcessor("/sdcard/test.flac"));
        }
        lingoRecorder.put(TIMER, new TimerProcessor(5000));
        lingoRecorder.put(SCORER, new LocalScorerProcessor(getApplication(), spokenText));


        lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {
            @Override
            public void onRecordStop(Throwable error, Result result) {
                if (error != null) {
                    Toast.makeText(DemoActivity.this, "录音出错\n" + Log.getStackTraceString(error),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DemoActivity.this, String.format("录音时长 = %d 毫秒", result.getDurationInMills()),
                            Toast.LENGTH_SHORT).show();
                }
                recordBtn.setText("start");
            }
        });

        lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onProcessStop(Throwable error, Map<String, AudioProcessor> map) {
                if (error != null) {
                    resultView.setText(Log.getStackTraceString(error));
                } else {
                    WavProcessor wavProcessor = (WavProcessor) map.get(WAV);
                    AndroidFlacProcessor flacProcessor = (AndroidFlacProcessor) map.get(FLAC);
                    LocalScorerProcessor scorerProcessor = (LocalScorerProcessor) map.get(SCORER);

                    StringBuilder sb = new StringBuilder();
                    if (wavProcessor != null) {
                        sb.append(String.format("wav file path = %s size = %s\n", wavProcessor.getFilePath()
                                , formatFileSize(wavProcessor.getFilePath())));
                    }
                    if (flacProcessor != null) {
                        sb.append(String.format("flac file path = %s size = %s \n", flacProcessor.getFilePath()
                                , formatFileSize(flacProcessor.getFilePath())));
                    }

                    if (scorerProcessor != null) {
                        sb.append(String.format("Got score = %d\n", scorerProcessor.getScore()));
                    }

                    resultView.setText(sb.toString());
                }
            }
        });


        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkRecordPermission()) {
                    return;
                }
                // isAvailable 为 false 为录音正在处理时，需要保护避免在这个时候操作录音器
                if (lingoRecorder.isAvailable()) {
                    if (lingoRecorder.isRecording()) {
                        lingoRecorder.stop();
                    } else {
                        // need get permission
                        lingoRecorder.start();
                        resultView.setText("");
                        recordBtn.setText("stop");
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.check_permission_fail, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            findViewById(R.id.recordBtn).performClick();
        }
    }

    private boolean checkRecordPermission() {
        if (PermissionChecker.checkSelfPermission(DemoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(DemoActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    new AlertDialog.Builder(DemoActivity.this)
                            .setTitle(R.string.check_permission_title)
                            .setMessage(R.string.check_permission_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_PERMISSION);
                                    }
                                }
                            }).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_PERMISSION);
                }
            }
            return false;
        }
        return true;
    }

    private static String formatFileSize(String path) {
        File file = new File(path);
        long size = file.length();
        String[] units = {"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024.0));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups)) + " " + units[digitGroups];
    }
}
