package es.usc.citius.servando.calendula.persistence;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Arrays;
import java.util.List;

/**
 * Created by joseangel.pineiro on 10/9/14.
 */
public class Testing {

    public static final String TAG = "Testing";

    public static void test() {

        Log.d(TAG, "================================================================");

        List<Routine> routines = Routine.findAll();

        for (Routine routine : routines) {
            Medicine med = routine.scheduleItems().get(0).schedule().medicine();
            Log.d(TAG, "Presentation of " + med.name() + ": " + med.presentation().name());
        }

        Routine r = Routine.findByName("Breakfast");

        if (r == null) {
            // create and save routine
            r = new Routine(LocalTime.now(), "Lunch");
            r.save();
        } else if (r.time() == null) {
            r.setTime(LocalTime.now());
            r.save();
        }

        // create and sve medicine
        Medicine m = new Medicine("Paracetamol", Presentation.EFFERVESCENT);
        m.save();

        // create and sve medicine
        Medicine m2 = new Medicine("Eferalgan", Presentation.CAPSULES);
        m2.save();

        // create and save schedule
        Schedule s = new Schedule(m);
        s.save();
        // create and save items
        ScheduleItem item = new ScheduleItem(s, r, 1);
        item.save();

        DailyScheduleItem dsi = new DailyScheduleItem(item.routine().time().toDateTimeToday(), item);
        dsi.save();


        for (Routine routine : routines) {
            Log.d(TAG, "Routine: " + Routine.findById(routine.getId()).name() + ", " + routine.time());
            for (ScheduleItem i : routine.scheduleItems()) {
                Log.d(TAG, " -- ScheduleItem: " + i.getId() + ", " + i.schedule().getId() + ", " + i.schedule().medicine().name());
            }

        }

        List<Medicine> medicines = Medicine.findAll();
        for (Medicine med : medicines) {
            Log.d(TAG, "Medicine: " + med.name());
        }


        List<Schedule> schedules = Schedule.findAll();
        for (Schedule schedule : schedules) {
            Log.d(TAG, "Schedule: " + schedule.medicine().name() + ", " + Arrays.toString(schedule.days()));
        }

        for (DailyScheduleItem d : DailyScheduleItem.fromDate(DateTime.now())) {
            Log.d(TAG, "DSI: " + d.toString());
        }
    }

}
