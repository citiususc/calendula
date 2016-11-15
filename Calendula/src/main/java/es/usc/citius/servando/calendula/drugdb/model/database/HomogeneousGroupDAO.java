package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.HomogeneousGroup;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class HomogeneousGroupDAO extends GenericDao<HomogeneousGroup, Long> {

    public static final String TAG = "HomogeneousGroupDAO";

    private Dao<HomogeneousGroup, Long> daoInstance = null;

    public HomogeneousGroupDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<HomogeneousGroup, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(HomogeneousGroup.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating HomogeneousGroup DAO", e);
            throw new RuntimeException("Error creating HomogeneousGroup DAO", e);
        }
    }

    public boolean empty() {
        return count() <= 0;
    }


}