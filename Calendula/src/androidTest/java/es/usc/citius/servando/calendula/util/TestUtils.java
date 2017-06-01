package es.usc.citius.servando.calendula.util;

import android.app.Activity;
import android.support.test.espresso.Espresso;
import android.view.WindowManager;

/**
 * Created by joseangel.pineiro on 3/31/15.
 */
public class TestUtils {

    public static void closeKeyboard() {
        Espresso.closeSoftKeyboard();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    public static void sleep(int ms) {
        Espresso.closeSoftKeyboard();
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }


    public static void unlockScreen(final Activity activity){
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
    }

}
