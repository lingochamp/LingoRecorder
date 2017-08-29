package com.liulishuo.engzo.lingorecorder.demo;

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
}
