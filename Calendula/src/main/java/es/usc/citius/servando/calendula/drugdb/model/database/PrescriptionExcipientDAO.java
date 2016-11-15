
package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionExcipient;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class PrescriptionExcipientDAO extends GenericDao<PrescriptionExcipient, Long> {

    public static final String TAG = "PrescriptionExDAO";

    private Dao<PrescriptionExcipient, Long> daoInstance = null;

    public PrescriptionExcipientDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<PrescriptionExcipient, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(PrescriptionExcipient.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating PrescriptionExcipient DAO", e);
            throw new RuntimeException("Error creating PrescriptionExcipient DAO", e);
        }
    }


}