package hcmute.edu.vn.note_taking.utils;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

public class PermissionUtils {
    public static boolean hasReadExternalStoragePermission(Context context) {
        return context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasWriteExternalStoragePermission(Context context) {
        return context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermission(Context context) {
        return context.checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static void requestReadExternalStoragePermission(Context context, int requestCode) {
        android.app.Activity activity = (android.app.Activity) context;
        activity.requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
    }

    public static void requestWriteExternalStoragePermission(Context context, int requestCode) {
        android.app.Activity activity = (android.app.Activity) context;
        activity.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
    }

    public static void requestCameraPermission(Context context, int requestCode) {
        android.app.Activity activity = (android.app.Activity) context;
        activity.requestPermissions(new String[]{android.Manifest.permission.CAMERA}, requestCode);
    }
}
