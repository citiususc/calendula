package es.usc.citius.servando.calendula.util;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by alvaro.brey.vilas on 7/11/16.
 */

public class KeyboardUtils {

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(
                activity.getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
    }
}
