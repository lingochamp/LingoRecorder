package com.liulishuo.engzo.lingorecorder.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by wcw on 3/28/17.
 */

public class LOG {

    public static final String TAG = "LingoRecorder";

    public static void d(String log) {
        if (!TextUtils.isEmpty(log)) {
            Log.d(TAG, log);
        }
    }

    public static void e(Throwable throwable) {
        if (throwable != null) {
            Log.e(TAG, Log.getStackTraceString(throwable));
        }
    }
}
