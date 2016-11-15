package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ContentUnit;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class ContentUnitDAO extends GenericDao<ContentUnit, Long> {

    public static final String TAG = "ContentUnitDAO";

    private Dao<ContentUnit, Long> daoInstance = null;

    public ContentUnitDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<ContentUnit, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(ContentUnit.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating ContentUnit DAO", e);
            throw new RuntimeException("Error creating ContentUnit DAO", e);
        }
    }


}