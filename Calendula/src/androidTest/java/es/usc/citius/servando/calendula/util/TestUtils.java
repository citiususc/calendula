package es.usc.citius.servando.calendula.util;

import android.support.test.espresso.Espresso;

/**
 * Created by joseangel.pineiro on 3/31/15.
 */
public class TestUtils {

    public static void closeKeyboard() {
        Espresso.closeSoftKeyboard();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }

    public static void sleep(int ms) {
        Espresso.closeSoftKeyboard();
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }
}
