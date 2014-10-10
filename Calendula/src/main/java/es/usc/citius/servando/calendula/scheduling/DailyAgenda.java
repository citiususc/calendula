package es.usc.citius.servando.calendula.scheduling;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Created by joseangel.pineiro on 10/10/14.
 */
public class DailyAgenda {

    public static final String TAG = DailyAgenda.class.getName();

    private static final String PREFERENCES_NAME = "DailyAgendaPreferences";
    private static final String PREF_LAST_DATE = "LastDate";

    public void setupForToday(Context ctx) {

        SharedPreferences settings = ctx.getSharedPreferences(PREFERENCES_NAME, 0);

        DateTime now = DateTime.now();
        Long lastDate = settings.getLong(PREF_LAST_DATE, 0);

        Log.d(TAG, "Setup daily agenda. Last updated: " + new DateTime(lastDate).toString("dd/MM - kk:mm"));
        Interval today = new Interval(now.withTimeAtStartOfDay(), now.withTimeAtStartOfDay().plusDays(1));

        // we need to update daily agenda
        if (!today.contains(lastDate)) {
            // Start transaction
            ActiveAndroid.beginTransaction();
            // delete old items
            DailyScheduleItem.removeAll();
            // and add new ones
            createDailySchedule(now);
            // Save last date to prefs
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(PREF_LAST_DATE, now.getMillis());
            editor.commit();
            // End transaction
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
        }
    }

    public void createDailySchedule(DateTime d) {
        // create a list with all day doses
        for (Routine r : Routine.findAll()) {
            for (ScheduleItem s : r.scheduleItems()) {
                s.schedule().enabledFor(d.getDayOfWeek());
                // create a dailyScheduleItem and save it
                new DailyScheduleItem(s).save();
            }
        }
    }

    public void updateDailySchedule(ScheduleItem item) {
        // Check if there is a daily schedule for this item
        DailyScheduleItem.findByScheduleItem(item);

    }

    // SINGLETON

    private static final DailyAgenda instance = new DailyAgenda();

    private DailyAgenda() {
    }

    public static final DailyAgenda instance() {
        return instance;
    }
}
