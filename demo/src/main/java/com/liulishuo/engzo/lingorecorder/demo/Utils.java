package com.liulishuo.engzo.lingorecorder.demo;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by rantianhua on 17/8/29.
 */

public class Utils {


    public static String getDurationString(long durationInMills) {
        if (durationInMills < 1000) {
            return durationInMills + "ms";
        } else if (durationInMills < 1000 * 60) {
            return (durationInMills / 1000) + "s";
        } else {
            return (durationInMills / 1000) + "min";
        }
    }

    public static String formatFileSize(String path) {
        File file = new File(path);
        long size = file.length();
        String[] units = {"B", "kB", "MB", "GB", "TB"};
        if (size > 0) {
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024.0));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups)) + " "
                    + units[digitGroups];
        } else {
            return "0B";
        }
    }

    public static MediaCodecInfo checkSupportMediaCodec(String mimeType) {
        if (Build.VERSION.SDK_INT < 16) {
            // TODO: 17/8/30 get codec list under api 16 https://stackoverflow
            // .com/questions/19992479/how-to-get-the-codec-list-on-android-4-0
            return null;
        }
        if (Build.VERSION.SDK_INT < 21) {
            final int count = MediaCodecList.getCodecCount();
            for (int i = 0; i < count; i++) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                if (!codecInfo.isEncoder()) {
                    continue;
                }
                if (checkIsSpecifyCodec(mimeType, codecInfo)) {
                    return codecInfo;
                }
            }
            return null;
        } else {
            final MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
            for (MediaCodecInfo codecInfo : mediaCodecList.getCodecInfos()) {
                if (checkIsSpecifyCodec(mimeType, codecInfo)) {
                    return codecInfo;
                }
            }
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static boolean checkIsSpecifyCodec(String mimeType, MediaCodecInfo mediaCodecInfo) {
        String[] types = mediaCodecInfo.getSupportedTypes();
        for (String type : types) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }
}
