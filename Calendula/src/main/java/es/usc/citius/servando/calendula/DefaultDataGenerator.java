package es.usc.citius.servando.calendula;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class DefaultDataGenerator {

    public static void fillDBWithDummyData(Context ctx) {

        Resources r = ctx.getResources();

//        if (Routine.findAll().isEmpty() && Schedule.findAll().isEmpty() && Medicine.findAll().isEmpty()) {
        if (Routine.findAll().size() == 0 && Schedule.findAll().size() == 0 && Medicine.findAll().size() == 0) {
            try {
                Log.d("DefaultDataGenerator", "Creating dummy data...");
                //ActiveAndroid.beginTransaction();
                new Routine(new LocalTime(9, 0), r.getString(R.string.routine_breakfast)).save();
                new Routine(new LocalTime(13, 0), r.getString(R.string.routine_lunch)).save();
                new Routine(new LocalTime(21, 0), r.getString(R.string.routine_dinner)).save();

//                Medicine sweet = new Medicine("Caramelos", Presentation.PILLS);
//                sweet.save();
//
//                Schedule s = new Schedule();
//                s.setDays(new boolean[]{true, true, true, true, true, true, true});
//                s.setMedicine(sweet);
//                s.save();
//
//                ScheduleItem i = new ScheduleItem(s, routine, 3.0f);
//                i.saveAndUpdateDailyAgenda();
                Log.d("DefaultDataGenerator", "Dummy data saved successfully!");
            } catch (Exception e) {
                Log.e("DefaultDataGenerator", "Error filling db with dummy data!", e);
            } finally {
                //ActiveAndroid.endTransaction();
            }
        }

    }

}
