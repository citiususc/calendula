package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Excipient;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class ExcipientDAO extends GenericDao<Excipient, Long> {

    public static final String TAG = "ExcipientDAO";

    private Dao<Excipient, Long> daoInstance = null;

    public ExcipientDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Excipient, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(Excipient.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating Excipient DAO", e);
            throw new RuntimeException("Error creating Excipient DAO", e);
        }
    }


}