package be.hogent.tarsos.dsp.util.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper class for managing audio permissions on Android.
 * 
 * @author Joren Six
 */
public class PermissionHelper {
    
    public static final int AUDIO_PERMISSION_REQUEST_CODE = 1001;
    
    /**
     * Check if RECORD_AUDIO permission is granted
     * 
     * @param context The context
     * @return true if permission is granted, false otherwise
     */
    public static boolean hasAudioPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Request RECORD_AUDIO permission
     * 
     * @param activity The activity to request permission from
     */
    public static void requestAudioPermission(Activity activity) {
        requestAudioPermission(activity, AUDIO_PERMISSION_REQUEST_CODE);
    }
    
    /**
     * Request RECORD_AUDIO permission with custom request code
     * 
     * @param activity The activity to request permission from
     * @param requestCode The request code for the permission
     */
    public static void requestAudioPermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                requestCode);
    }
    
    /**
     * Check if permission should show rationale
     * 
     * @param activity The activity
     * @return true if rationale should be shown, false otherwise
     */
    public static boolean shouldShowRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.RECORD_AUDIO);
    }
    
    /**
     * Check if permission request was granted
     * 
     * @param grantResults The grant results from onRequestPermissionsResult
     * @return true if permission was granted, false otherwise
     */
    public static boolean isPermissionGranted(int[] grantResults) {
        return grantResults.length > 0 && 
               grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}
