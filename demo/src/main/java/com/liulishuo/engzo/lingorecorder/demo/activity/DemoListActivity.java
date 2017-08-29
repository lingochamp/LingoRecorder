package com.liulishuo.engzo.lingorecorder.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

}
