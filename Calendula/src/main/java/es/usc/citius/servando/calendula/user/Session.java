package es.usc.citius.servando.calendula.user;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by joseangel.pineiro on 6/16/14.
 */
public class Session {

    private static String SESSION_FILENAME = "session.json";

    private static Session instance = new Session();


    User user;

    private Session() {
    }

    public static Session getInstance() {
        return instance;
    }

    public User getUser() {
        return user;
    }

    public boolean isOpen() {
        return user != null;
    }

    public void close(Context context) {
        // delete user data file
        context.deleteFile(SESSION_FILENAME);

    }

    public void create(Context context, User user) throws Exception {
        // open session file where user data is stored
        final FileOutputStream out = context.openFileOutput(SESSION_FILENAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(user);
        out.write(json.getBytes());
        out.close();

        open(context);
    }

    public void open(Context context) throws Exception {
        // open session file where user data is stored
        try {
            final FileInputStream is = context.openFileInput(SESSION_FILENAME);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            user = new Gson().fromJson(reader, User.class);

        } catch (FileNotFoundException e) {
            Log.e(Session.class.getName(), "Error reading session data");
            throw new Exception("No user data file was found");
        }

    }

}
