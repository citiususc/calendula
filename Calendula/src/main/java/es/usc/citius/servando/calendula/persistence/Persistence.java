package es.usc.citius.servando.calendula.persistence;

import com.activeandroid.Model;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.events.PersistenceEvents;

/**
 * Created by joseangel.pineiro on 11/4/14.
 */
public class Persistence {

    // CREATE OR UPDTAE 

    public void save(Routine m) {
        saveModel(m);
        CalendulaApp.eventBus().post(PersistenceEvents.ROUTINE_EVENT);
    }

    public void save(Medicine m) {
        saveModel(m);
        CalendulaApp.eventBus().post(PersistenceEvents.MEDICINE_EVENT);
    }

    public void save(Schedule m) {
        saveModel(m);
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

    // PRIVATE METHODS

    private void saveModel(Model m) {
        m.save();
    }


    // SIngleton

    private static final Persistence instance = new Persistence();

    public static final Persistence instance() {
        return instance;
    }
}
