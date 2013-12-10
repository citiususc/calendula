package es.usc.citius.servando.calendula;

import org.joda.time.DateTime;

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

        RoutineStore.getInstance().addRoutine(new Routine(DateTime.now().minusHours(5).toLocalTime(), "Breakfast"));
        RoutineStore.getInstance().addRoutine(new Routine(DateTime.now().toLocalTime(), "Lunch"));
        RoutineStore.getInstance().addRoutine(new Routine(DateTime.now().plusHours(7).toLocalTime(), "Dinner"));

    }

    public static void fillMedicineStore() {

        MedicineStore.getInstance().addMedicine(new Medicine("Atrovent", Presentation.PILLS));
        MedicineStore.getInstance().addMedicine(new Medicine("Ramipril", Presentation.EFFERVESCENT));
        MedicineStore.getInstance().addMedicine(new Medicine("Digoxina", Presentation.DROPS));
        MedicineStore.getInstance().addMedicine(new Medicine("Ibuprofeno", Presentation.CAPSULES));

    }
}
