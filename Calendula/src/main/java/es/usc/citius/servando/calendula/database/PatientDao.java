package es.usc.citius.servando.calendula.database;

import android.content.Context;
import android.preference.PreferenceManager;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Patient;

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
    public void fireEvent() {
        CalendulaApp.eventBus().post(PersistenceEvents.USER_EVENT);
    }

    /// Mange active patient through preferences

    public boolean isActive(Patient p, Context ctx){
        Long activeId =  PreferenceManager.getDefaultSharedPreferences(ctx).getLong(PREFERENCE_ACTIVE_PATIENT,-1);
        return activeId.equals(p.id());
    }

    public Patient getActive(Context ctx){
        long id =  PreferenceManager.getDefaultSharedPreferences(ctx).getLong(PREFERENCE_ACTIVE_PATIENT,-1);
        if(id != -1){
            return findById(id);
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
}
