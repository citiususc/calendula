package es.usc.citius.servando.calendula;

import android.app.Application;
import android.content.Context;

import es.usc.citius.servando.calendula.store.MedicineStore;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.store.ScheduleStore;
import es.usc.citius.servando.calendula.util.Settings;

/**
 * Created by castrelo on 4/10/14.
 */
public class CalendulaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Context ctx = getApplicationContext();
        try {
            // Load settings
            Settings.instance().load(ctx);
            // Load medicines
            MedicineStore.instance().load(ctx);
            // load routines
            RoutineStore.instance().load(ctx);
            // Load schedules
            ScheduleStore.instance().load(ctx);
            // Update daily checker
            DailyDosageChecker.instance().updateDailySchedule(ctx);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
