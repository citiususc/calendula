package es.usc.citius.servando.calendula.util;

import android.app.Activity;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

/**
 * Created by joseangel.pineiro on 2/17/15.
 */
public class Snack {

    public static void show(String string, Activity activity, com.nispok.snackbar.Snackbar.SnackbarDuration duration) {

        SnackbarManager.show(com.nispok.snackbar.Snackbar.with(activity.getApplicationContext())
                .type(SnackbarType.MULTI_LINE)
                .duration(duration)
                .text(string)
                , activity);
    }

    public static void show(String string, Activity activity) {
        show(string, activity, Snackbar.SnackbarDuration.LENGTH_SHORT);
    }

    public static void show(int string, Activity activity) {
        show(activity.getResources().getString(string), activity, Snackbar.SnackbarDuration.LENGTH_SHORT);
    }

}

