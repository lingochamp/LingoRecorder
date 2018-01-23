package com.liulishuo.engzo.lingorecorder.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liulishuo.engzo.lingorecorder.LingoRecorder;
import com.liulishuo.engzo.lingorecorder.demo.LocalScorerProcessor;
import com.liulishuo.engzo.lingorecorder.demo.R;
import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;

import java.util.Map;

/**
 * demonstrate across process processor
 */

public class AcrossProcessDemonstrateActivity extends RecordActivity {

    public static final String SCORER = "localScorer";

    private Button btnScorer;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_across_process_demonstrate);

        btnScorer = (Button) findViewById(R.id.btn_scorer);
        tvResult = (TextView) findViewById(R.id.tv_scorer_result);
        tvResult.setText(getString(R.string.scorer_result, ""));

        btnScorer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkRecordPermission()) return;
                if (!lingoRecorder.isAvailable()) return;
                if (lingoRecorder.isRecording()) {
                    lingoRecorder.stop();
                } else {
                    lingoRecorder.start();
                    tvResult.setText(getString(R.string.scorer_result, ""));
                    btnScorer.setText(R.string.stop_scorer);
                }
            }
        });

        final String spokenText = getString(R.string.scorer_sentence);
        lingoRecorder.put(SCORER, new LocalScorerProcessor(getApplication(), spokenText));
    }

    @Override
    protected void onProcessError(Throwable throwable) {
        super.onProcessError(throwable);
        btnScorer.setText(R.string.start_record);
    }

    @Override
    protected void onRecordError(Throwable throwable) {
        super.onRecordError(throwable);
        btnScorer.setText(R.string.start_record);
    }

    @Override
    protected void onProcessStop(Map<String, AudioProcessor> map) {
        btnScorer.setText(R.string.start_record);
        LocalScorerProcessor localScorerProcessor = (LocalScorerProcessor) map.get(SCORER);
        tvResult.setText(
                getString(R.string.scorer_result, String.valueOf(localScorerProcessor.getScore())));
    }

    @Override
    protected void onRecordStop(LingoRecorder.OnRecordStopListener.Result result) {

    }

    @Override
    protected void onPermissionGranted() {
        btnScorer.performClick();
    }
}
