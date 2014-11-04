package es.usc.citius.servando.calendula.events;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;

/**
 * Created by joseangel.pineiro on 11/4/14.
 */
public class PersistenceEvents {

    public static class ModelCreateOrUpdateEvent {
        public Class<?> clazz;

        public ModelCreateOrUpdateEvent(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    public static ModelCreateOrUpdateEvent ROUTINE_EVENT = new ModelCreateOrUpdateEvent(Routine.class);
    public static ModelCreateOrUpdateEvent MEDICINE_EVENT = new ModelCreateOrUpdateEvent(Medicine.class);
    public static ModelCreateOrUpdateEvent SCHEDULE_EVENT = new ModelCreateOrUpdateEvent(Schedule.class);

}
