package es.usc.citius.servando.calendula.persistence;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;

/**
 * Created by joseangel.pineiro on 11/4/14.
 */
@Deprecated
public class Persistence {

    // CREATE OR UPDTAE 

    public void save(Routine m) {
        DB.Routines.save(m);
        CalendulaApp.eventBus().post(PersistenceEvents.ROUTINE_EVENT);
    }

    public void save(Medicine m) {
        DB.Medicines.save(m);
        CalendulaApp.eventBus().post(PersistenceEvents.MEDICINE_EVENT);
    }

    public void save(Schedule m) {
        DB.Schedules.save(m);
        CalendulaApp.eventBus().post(PersistenceEvents.SCHEDULE_EVENT);
    }

    // DELETE 

    public void deleteCascade(Schedule m) {
        m.deleteCascade();
        CalendulaApp.eventBus().post(PersistenceEvents.SCHEDULE_EVENT);
    }

    public void deleteCascade(Medicine m) {
        m.deleteCascade();
        CalendulaApp.eventBus().post(PersistenceEvents.MEDICINE_EVENT);
    }

    public void deleteCascade(Routine m) {
        m.deleteCascade();
        CalendulaApp.eventBus().post(PersistenceEvents.ROUTINE_EVENT);
    }

    // SIngleton

    private static final Persistence instance = new Persistence();

    public static final Persistence instance() {
        return instance;
    }
}
