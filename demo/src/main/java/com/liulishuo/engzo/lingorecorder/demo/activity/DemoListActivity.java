package com.liulishuo.engzo.lingorecorder.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liulishuo.engzo.lingorecorder.demo.R;

/**
 * Created by rantianhua on 17/8/29.
 * show demo list
 */

public class DemoListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_list);
    }

    public void recordDemonstrate(View view) {
        startActivity(new Intent(this, RecordDemonstrateActivity.class));
    }

    public void processorsDemonstrate(View view) {
        startActivity(new Intent(this, ProcessorsDemonstrateActivity.class));
    }

    public void flacDemonstrate(View view) {
        startActivity(new Intent(this, FlacDemonstrateActivity.class));
    }

    public void acrossProcessDemonstrate(View view) {
        startActivity(new Intent(this, AcrossProcessDemonstrateActivity.class));
    }

    public void volumeDemonstrate(View view) {
        startActivity(new Intent(this, VolumeDemonstrateActivity.class));
    }

}
