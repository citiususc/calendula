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
import java.util.List;

import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.util.GsonUtil;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class ScheduleStore extends Store {

    private static final String SCHEDULES_FILE_NAME = "schedules.json";

    List<Schedule> schedules;

    private static ScheduleStore instance = null;

    public static ScheduleStore instance() {
        if (instance == null) {
            instance = new ScheduleStore();
        }
        return instance;
    }

    public ScheduleStore() {
        schedules = new ArrayList<Schedule>();
    }

    public void addSchedule(Schedule r) {
        schedules.add(r);
        notifyDataChange();
    }

    public void removeSchedule(Schedule r) {
        schedules.remove(r);
        notifyDataChange();
    }

    public void removeFromRoutine(Routine r) {
        for (Schedule s : schedules) {
            ArrayList<ScheduleItem> toDelete = new ArrayList<ScheduleItem>();
            for (ScheduleItem i : s.items()) {
                if (r.id().equals(i.routineId())) {
                    toDelete.add(i);
                }
            }
            s.removeItems(toDelete);
        }
        notifyDataChange();
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public int size() {
        return schedules.size();
    }

    public void save(Context context) {
        // open session file where user data is stored
        FileOutputStream out = null;
        try {
            out = context.openFileOutput(SCHEDULES_FILE_NAME, Context.MODE_PRIVATE);
            String json = GsonUtil.get().toJson(schedules);
            out.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
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
            is = context.openFileInput(SCHEDULES_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            List<Schedule> schedules = GsonUtil.get().fromJson(reader, new TypeToken<List<Schedule>>() {
            }.getType());
            if (schedules != null) {
                this.schedules = schedules;
            }
            Log.d(ScheduleStore.class.getName(), "Schedules loaded");
            Log.d(ScheduleStore.class.getName(), GsonUtil.get().toJson(this.schedules));
        } catch (Exception e) {
            removeAll(context);
            Log.e(ScheduleStore.class.getName(), "Error reading schedules file", e);
        } finally {

            try {
                is.close();
            } catch (Exception unhandled) {
                //do nothing
            }
        }

    }

    public void removeAll(Context context) {
        schedules.clear();
        context.deleteFile(SCHEDULES_FILE_NAME);
        notifyDataChange();
    }


}
