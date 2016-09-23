package es.usc.citius.servando.calendula.database;

import android.content.Context;
import android.preference.PreferenceManager;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class PatientDao extends GenericDao<Patient, Long> {

    public static final String PREFERENCE_ACTIVE_PATIENT = "active_patient";

    public static final String TAG = "PatientDao";

    public PatientDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Patient, Long> getConcreteDao() {
        try {
            return dbHelper.getPatientDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating patients dao", e);
        }
    }

    @Override
    public void saveAndFireEvent(Patient p) {

        Object event =  p.id() == null ? new PersistenceEvents.UserCreateEvent(p) : new PersistenceEvents.UserUpdateEvent(p);
        save(p);
        CalendulaApp.eventBus().post(event);

    }

    /// Mange active patient through preferences

    public boolean isActive(Patient p, Context ctx){
        Long activeId =  PreferenceManager.getDefaultSharedPreferences(ctx).getLong(PREFERENCE_ACTIVE_PATIENT,-1);
        return activeId.equals(p.id());
    }

    public Patient getActive(Context ctx){
        long id =  PreferenceManager.getDefaultSharedPreferences(ctx).getLong(PREFERENCE_ACTIVE_PATIENT,-1);
        Patient p;
        if(id != -1){
            p = findById(id);
            if(p == null) {
                p = getDefault();
                setActive(p,ctx);
            }
            return p;
        }else{
            return getDefault();
        }
    }

    public Patient getDefault() {
        return findOneBy(Patient.COLUMN_DEFAULT, true);
    }

    public void setActive(Patient patient, Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        .putLong(PREFERENCE_ACTIVE_PATIENT,patient.id())
        .commit();
        CalendulaApp.eventBus().post(new PersistenceEvents.ActiveUserChangeEvent(patient));
    }
    public void setActiveById(Long id, Context ctx) {
        Patient patient = findById(id);
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putLong(PREFERENCE_ACTIVE_PATIENT, patient.id())
                .commit();
        CalendulaApp.eventBus().post(new PersistenceEvents.ActiveUserChangeEvent(patient));
    }


    public void removeCascade(Patient p) {
        // remove all data
        removeAllStuff(p);
        // remove patient
        DB.patients().remove(p);

    }

    public void removeAllStuff(Patient p) {
        for(Medicine m : DB.medicines().findAll()){
            if(m.patient().id() == p.id()){
                // this also remove schedules
                DB.medicines().deleteCascade(m, true);
            }
        }
        // remove routines
        for(Routine r:  DB.routines().findAll()) {
            if (r.patient().id() == p.id()) {
                DB.routines().remove(r);
            }
        }
    }
}
