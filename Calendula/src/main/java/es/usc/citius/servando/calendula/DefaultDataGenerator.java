package es.usc.citius.servando.calendula;

import android.content.Context;
import android.content.res.Resources;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.persistence.Routine;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class DefaultDataGenerator {

    public static void fillRoutineStore(Context ctx) {

        Resources r = ctx.getResources();

        new Routine(new LocalTime(9, 0), r.getString(R.string.routine_breakfast)).save();
        new Routine(new LocalTime(13, 0), r.getString(R.string.routine_lunch)).save();
        new Routine(new LocalTime(21, 0), r.getString(R.string.routine_dinner)).save();

    }

}
