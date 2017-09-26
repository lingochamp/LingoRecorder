package com.liulishuo.engzo.lingorecorder.demo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by rantianhua on 17/8/29.
 */

public class RecordPermissionHelper {

    private static final int REQUEST_CODE_PERMISSION = 10010;

    private final AppCompatActivity activity;

    private PermissionGrantedListener grantedListener;

    public RecordPermissionHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setGrantedListener(
            PermissionGrantedListener grantedListener) {
        this.grantedListener = grantedListener;
    }

    public boolean checkRecordPermission() {
        if (PermissionChecker.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity.shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        activity.shouldShowRequestPermissionRationale(
                                Manifest.permission.RECORD_AUDIO)) {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.check_permission_title)
                            .setMessage(R.string.check_permission_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.confirm,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                activity.requestPermissions(new String[]{
                                                                Manifest.permission
                                                                        .WRITE_EXTERNAL_STORAGE,
                                                                Manifest.permission.RECORD_AUDIO},
                                                        REQUEST_CODE_PERMISSION);
                                            }
                                        }
                                    }).show();
                } else {
                    activity.requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_PERMISSION);
                }
            }
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity, R.string.check_permission_fail,
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (grantedListener != null) {
                grantedListener.onPermissionGranted();
            }
        }
    }

    public interface PermissionGrantedListener {
        void onPermissionGranted();
    }
}
