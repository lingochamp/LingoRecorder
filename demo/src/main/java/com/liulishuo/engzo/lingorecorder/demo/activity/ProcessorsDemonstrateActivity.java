package com.liulishuo.engzo.lingorecorder.demo.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.liulishuo.engzo.lingorecorder.LingoRecorder;
import com.liulishuo.engzo.lingorecorder.demo.R;
import com.liulishuo.engzo.lingorecorder.processor.AudioProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by rantianhua on 17/8/29.
 * demonstrate how to custom processor for record data
 */

public class ProcessorsDemonstrateActivity extends RecordActivity {

    private static final String PROCESSOR_1 = "processor1";
    private static final String PROCESSOR_2 = "processor2";

    private TextView tvProcessor1;
    private TextView tvProcessor2;
    private RecyclerView rcv1;
    private RecyclerView rcv2;
    private Button btn;

    private Handler handler;

    private ProcessorAdapter adapter1;
    private ProcessorAdapter adapter2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processors_demonstrate);

        tvProcessor1 = (TextView) findViewById(R.id.tv_processor_1_title);
        tvProcessor2 = (TextView) findViewById(R.id.tv_processor_2_title);
        tvProcessor1.setText(getString(R.string.processor_1_title, ""));
        tvProcessor2.setText(getString(R.string.processor_2_title, ""));

        rcv1 = (RecyclerView) findViewById(R.id.rcv_processor_1);
        rcv2 = (RecyclerView) findViewById(R.id.rcv_processor_2);
        adapter1 = new ProcessorAdapter();
        adapter2 = new ProcessorAdapter();
        rcv1.setLayoutManager(new LinearLayoutManager(this));
        rcv1.setHasFixedSize(true);
        rcv1.setAdapter(adapter1);
        rcv2.setLayoutManager(new LinearLayoutManager(this));
        rcv2.setHasFixedSize(true);
        rcv2.setAdapter(adapter2);

        btn = (Button) findViewById(R.id.btn_record);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkRecordPermission()) return;
                if (lingoRecorder.isRecording()) {
                    lingoRecorder.stop();
                } else if (!lingoRecorder.isProcessing()){
                    lingoRecorder.start();
                    btn.setText(R.string.stop_record);
                }
            }
        });

        initHandler();

        initLingoRecorder();
    }

    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (isFinishing()) return false;
                final String data = (String) msg.obj;
                if (msg.what == 1) {
                    adapter1.addData(data);
                    rcv1.scrollToPosition(adapter1.getItemCount() - 1);
                } else if (msg.what == 2) {
                    adapter2.addData(data);
                    rcv2.scrollToPosition(adapter2.getItemCount() - 1);
                }
                return false;
            }
        });
    }

    private void initLingoRecorder() {
        lingoRecorder.put(PROCESSOR_1, new

                AudioProcessor() {

                    private int item;

                    @Override
                    public void start() throws Exception {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvProcessor1.setText(
                                        getString(R.string.processor_1_title, "start"));
                            }
                        });
                    }

                    @Override
                    public void flow(byte[] bytes, int size) throws Exception {
                        final Message message = handler.obtainMessage();
                        message.what = 1;
                        message.obj = String.format(Locale.CHINA,
                                "handle flow item, position at %d, size is %d", (++item), size);
                        handler.sendMessage(message);
                        if (item == 1) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tvProcessor1.setText(
                                            getString(R.string.processor_1_title, "processing..."));
                                }
                            });
                        }
                    }

                    @Override
                    public boolean needExit() {
                        return false;
                    }

                    @Override
                    public void end() throws Exception {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvProcessor1.setText(getString(R.string.processor_1_title, "stop"));
                            }
                        });
                    }

                    @Override
                    public void release() {
                        item = 0;
                    }
                });
        lingoRecorder.put(PROCESSOR_2, new

                AudioProcessor() {

                    private int item;

                    @Override
                    public void start() throws Exception {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvProcessor2.setText(
                                        getString(R.string.processor_2_title, "start"));
                            }
                        });
                    }

                    @Override
                    public void flow(byte[] bytes, int size) throws Exception {
                        final Message message = handler.obtainMessage();
                        message.what = 2;
                        message.obj = String.format(Locale.CHINA,
                                "handle flow item, position at %d, size is %d", (++item), size);
                        handler.sendMessage(message);
                        if (item == 1) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tvProcessor2.setText(
                                            getString(R.string.processor_2_title, "processing..."));
                                }
                            });
                        }
                    }

                    @Override
                    public boolean needExit() {
                        return false;
                    }

                    @Override
                    public void end() throws Exception {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvProcessor2.setText(getString(R.string.processor_2_title, "stop"));
                            }
                        });
                    }

                    @Override
                    public void release() {
                        item = 0;
                    }
                });
    }

    @Override
    protected void onProcessStop(Map<String, AudioProcessor> map) {
        btn.setText(R.string.start_record);
    }

    @Override
    protected void onRecordStop(LingoRecorder.OnRecordStopListener.Result result) {

    }

    @Override
    protected void onPermissionGranted() {
        btn.performClick();
    }

    static class ProcessorAdapter extends RecyclerView.Adapter<ProcessorAdapter.VH> {

        private final List<String> data;

        ProcessorAdapter() {
            this.data = new ArrayList<>();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent,
                int viewType) {
            final TextView textView = new TextView(parent.getContext());
            textView.setTextColor(Color.BLACK);
            textView.setPadding(0, dpToPx(parent.getContext(), 5), 0,
                    dpToPx(parent.getContext(), 5));
            textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            return new VH(textView);
        }

        int dpToPx(Context context, int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                    context.getResources().getDisplayMetrics());
        }

        @Override
        public void onBindViewHolder(VH holder,
                int position) {
            holder.tv.setText(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        void addData(String item) {
            if (data.size() > 20) {
                data.remove(0);
            }
            data.add(item);
            notifyDataSetChanged();
        }

        static class VH extends RecyclerView.ViewHolder {

            TextView tv;

            VH(View itemView) {
                super(itemView);
                tv = (TextView) itemView;
            }
        }
    }
}
