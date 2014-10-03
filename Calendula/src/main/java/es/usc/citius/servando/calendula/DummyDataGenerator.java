package es.usc.citius.servando.calendula;

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

    public static void fillRoutineStore() {

        //RoutineStore.instance().addRoutine(new Routine(new LocalTime(9, 0), "Breakfast"));
        //RoutineStore.instance().addRoutine(new Routine(new LocalTime(13, 0), "Lunch"));
        //RoutineStore.instance().addRoutine(new Routine(new LocalTime(21, 0), "Dinner"));

    }

    public static void fillMedicineStore() {

        MedicineStore.getInstance().addMedicine(new Medicine("Atrovent", Presentation.PILLS));
        MedicineStore.getInstance().addMedicine(new Medicine("Ramipril", Presentation.EFFERVESCENT));
        MedicineStore.getInstance().addMedicine(new Medicine("Digoxina", Presentation.DROPS));
        MedicineStore.getInstance().addMedicine(new Medicine("Ibuprofeno", Presentation.CAPSULES));

    }
}
