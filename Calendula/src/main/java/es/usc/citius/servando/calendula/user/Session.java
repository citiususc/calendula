package es.usc.citius.servando.calendula.user;

import android.content.Context;

/**
 * Created by joseangel.pineiro on 6/16/14.
 */
public class Session {

    private static String SESSION_FILENAME = "session.json";

    private static Session instance = new Session();

    private static boolean isOpen = false;

    private Session() {
    }

    public static Session instance() {
        return instance;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void close(Context context) {
        isOpen = false;
    }

    public boolean open(Context context) throws Exception {
        isOpen = true;
        return true;
    }

}
