package es.usc.citius.servando.calendula;

import android.content.Context;
import android.content.res.Resources;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.model.Presentation;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.store.MedicineStore;
import es.usc.citius.servando.calendula.store.RoutineStore;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class DummyDataGenerator {

    public static void fillRoutineStore(Context ctx) {

        Resources r = ctx.getResources();
        RoutineStore.instance().addRoutine(new Routine(new LocalTime(9, 0), r.getString(R.string.routine_breakfast)));
        RoutineStore.instance().addRoutine(new Routine(new LocalTime(13, 0), r.getString(R.string.routine_lunch)));
        RoutineStore.instance().addRoutine(new Routine(new LocalTime(21, 0), r.getString(R.string.routine_dinner)));
        RoutineStore.instance().save(ctx);
    }

    public static void fillMedicineStore(Context ctx) {

        MedicineStore.instance().addMedicine(new Medicine("Atrovent", Presentation.PILLS));
        MedicineStore.instance().addMedicine(new Medicine("Ramipril", Presentation.EFFERVESCENT));
        MedicineStore.instance().addMedicine(new Medicine("Digoxina", Presentation.DROPS));
        MedicineStore.instance().addMedicine(new Medicine("Ibuprofeno", Presentation.CAPSULES));

    }
}
