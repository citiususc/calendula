package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class PrescriptionDAO extends GenericDao<Prescription, Long> {

    public static final String TAG = "PrescriptionDAO";

    private Dao<Prescription, Long> daoInstance = null;

    public PrescriptionDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Prescription, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(Prescription.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating Prescription DAO", e);
            throw new RuntimeException("Error creating Prescription DAO", e);
        }
    }

    public boolean empty() {
        return count() <= 0;
    }

    public List<Prescription> findByName(final String name, final int limit) {
        Log.d("Prescription", "Query by name: " + name);
        return like(Prescription.COLUMN_NAME, name + "%", (long) limit);
    }

    public Prescription findByCn(final String code) {
        return findOneBy(Prescription.COLUMN_CODE, code);
    }


}