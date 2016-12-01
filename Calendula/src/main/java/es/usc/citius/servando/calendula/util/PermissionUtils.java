package es.usc.citius.servando.calendula.util;

/**
 *
 */
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;

/**
 * Android Permission Utility class. A collection of static convenience methods
 * for checking if Android Runtime permissions are required.
 *
 * https://gist.github.com/mtsahakis/bd54dc595f8cf170eb1bce17f31722b5
 */
public class PermissionUtils {

    public static boolean useRunTimePermissions() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean hasPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestPermissions(Activity activity, String[] permission, int requestCode) {
        if (useRunTimePermissions()) {
            activity.requestPermissions(permission, requestCode);
        }
    }

    public static void requestPermissions(Fragment fragment, String[] permission, int requestCode) {
        if (useRunTimePermissions()) {
            fragment.requestPermissions(permission, requestCode);
        }
    }

    public static boolean shouldShowRational(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    public static boolean shouldAskForPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return !hasPermission(activity, permission) &&
                    (!hasAskedForPermission(activity, permission) ||
                            shouldShowRational(activity, permission));
        }
        return false;
    }

    public static void goToAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static boolean hasAskedForPermission(Activity activity, String permission) {
        return PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getBoolean("asked-for" + permission, false);
    }

    public static void markedPermissionAsAsked(Activity activity, String permission) {
        PreferenceManager
                .getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean("asked-for" + permission, true)
                .commit();
    }
}