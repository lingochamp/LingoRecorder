package com.liulishuo.engzo.lingorecorder.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rantianhua on 2017/9/27.
 * show the change of volume
 */

public class VolumeView extends View {

    private Path path;
    private double amplitude;
    private int step = 3;
    private Paint paint;
    private Handler handler;
    private Runnable waveRunnable;
    private long startTime;
    private List<Float> xCalculateSamples;
    private List<Integer> originX;

    public VolumeView(Context context) {
        super(context);
        init();
    }

    public VolumeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VolumeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VolumeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        path = new Path();

        handler = new Handler();
        waveRunnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
                startWave();
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();

        path.reset();
        if (xCalculateSamples == null) {
            calculateSample(width);
        }
        final int ampl = (int) (amplitude * (height / 2));
        final float offset = ((System.currentTimeMillis() - startTime) / 200f) % 2;

        path.moveTo(originX.get(0), height / 2);
        for (int i = 0; i < xCalculateSamples.size(); i++) {
            final float x = originX.get(i);
            final float y = (float) (0.75 * Math.sin(xCalculateSamples.get(i) * Math.PI - offset * Math.PI) * (4 / (4 + Math.pow(xCalculateSamples.get(i), 2))) * ampl);
            path.lineTo(x, y + height / 2);
        }

        canvas.drawPath(path, paint);
    }

    private void calculateSample(int width) {
        xCalculateSamples = new ArrayList<>();
        originX = new ArrayList<>();
        for (int i = 0; i <= width; i += step) {
            xCalculateSamples.add((i / (float) width) * 4 - 2);
            originX.add(i);
        }
    }

    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    public void startWave() {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        handler.postDelayed(waveRunnable, 16);
    }

    public void stopWave() {
        handler.removeCallbacks(waveRunnable);
        setAmplitude(0);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopWave();
        super.onDetachedFromWindow();
    }
}
