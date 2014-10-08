package es.usc.citius.servando.calendula.store;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.util.GsonUtil;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class RoutineStore extends Store {

    public static final String TAG = RoutineStore.class.getName();
    private static final String ROUTINES_FILE_NAME = "routines.json";

    HashMap<String, Routine> routines;

    private static final RoutineStore instance = new RoutineStore();

    public static RoutineStore instance() {
        return instance;
    }

    public RoutineStore() {
        routines = new HashMap<String, Routine>();
    }


    public void addRoutine(Routine r) {
        routines.put(r.id(), r);
    }


    public Routine get(String id) {
        return routines.get(id);
    }

    public Routine getRoutineByName(String name) {

        if (name == null) {
            return null;
        }

        for (Routine r : routines.values()) {
            if (name.equalsIgnoreCase(r.getName())) {
                return r;
            }
        }
        return null;
    }


    public Routine getRoutine(String timeAsString) {
        if (timeAsString == null) {
            return null;
        }

        for (Routine r : routines.values()) {
            if (timeAsString.equalsIgnoreCase(r.getTimeAsString())) {
                return r;
            }
        }
        return null;
    }

    public List<Routine> getInHour(int hour) {

        List<Routine> inHour = new ArrayList<Routine>();

        for (Routine r : routines.values()) {
            if (r.getTime().getHourOfDay() == hour) {
                inHour.add(r);
            }
        }
        return inHour;
    }


    public void removeRoutine(Routine r) {
        //remove all schedules bound to routine
        ScheduleStore.instance().removeFromRoutine(r);
        routines.remove(r.id());
    }

    public List<Routine> asList() {
        List<Routine> rs = new ArrayList<Routine>(routines.values());
        Collections.sort(rs);
        return rs;
    }


    public int size() {
        return routines.size();
    }


    public void save(Context context) {
        // open session file where user data is stored
        FileOutputStream out = null;
        try {
            out = context.openFileOutput(ROUTINES_FILE_NAME, Context.MODE_PRIVATE);
            String json = GsonUtil.get().toJson(routines);
            out.write(json.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error saving routines", e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                // do nothing
            }
        }

    }

    public void load(Context context) throws Exception {
        // open session file where user data is stored
        FileInputStream is = null;
        try {
            is = context.openFileInput(ROUTINES_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            HashMap<String, Routine> routines = GsonUtil.get().fromJson(reader, new TypeToken<HashMap<String, Routine>>() {
            }.getType());
            if (routines != null) {
                this.routines = routines;
            }
            Log.d(TAG, "Routines loaded (" + routines.size() + ")");
            Log.d(TAG, GsonUtil.get().toJson(this.routines));
        } catch (Exception e) {
            removeAll(context);
            Log.e(ScheduleStore.class.getName(), "Error reading routines file", e);
        } finally {
            try {
                is.close();
            } catch (Exception unhandled) {
                //do nothing
            }
        }
    }

    public void removeAll(Context context) {
        routines.clear();
        context.deleteFile(ROUTINES_FILE_NAME);
        notifyDataChange();
    }

}
