package es.usc.citius.servando.calendula.user;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import es.usc.citius.servando.calendula.AlarmScheduler;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.store.ScheduleStore;
import es.usc.citius.servando.calendula.util.api.ApiRequestBuilder;
import es.usc.citius.servando.calendula.util.api.ApiResponse;

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
        user=null;
        AlarmScheduler.instance().cancelDailyAlarms(context);
        context.deleteFile(SESSION_FILENAME);
    }

    public boolean resume(Context context) throws Exception {

        ApiResponse response = null;

        try {
            open(context);

        response = new ApiRequestBuilder()
                    .to("auth")
                    .authorize(user.getToken())
                    .expect(ApiResponse.class)
                    .post();

            Log.d("Session", "Resume session [" + response.success + ", " +response.status + "]");
            return response.success;

        }catch (Exception e){
            Log.e("Session", "Cannot resume user session [" + response + "]",e);
            try {
                // close(context);
            }catch (Exception unhandled){/* do nothintg */}
        }

        return false;
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
        FileInputStream is = null;
        try {
            is = context.openFileInput(SESSION_FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            user = new Gson().fromJson(reader, User.class);
            onCreateSession(context);
            Log.d(Session.class.getName(), "Opening session for user [" + user.getEmail()+ ", " + user.getToken() + "]");
        } catch (FileNotFoundException e) {
            Log.e(Session.class.getName(), "Error reading session data",e);
            throw new Exception("No user data file was found");
        }finally {
            try {
                is.close();
            }catch (Exception unhandled){
                //do nothing
            }
        }

    }

    private void onCreateSession(Context context) throws Exception{

        // load routines
        RoutineStore.instance().load(context);
        // Load schedules
        ScheduleStore.instance().load(context);
        // Schedule routine alarms
        AlarmScheduler.instance().scheduleDailyAlarms(context);
    }

    private String generateUserDirName(String username){
        return username.replace("@","").replace(".","-");
    }

}
