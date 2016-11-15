package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PackageType;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class PackageTypeDAO extends GenericDao<PackageType, Long> {

    public static final String TAG = "PackageTypeDAO";

    private Dao<PackageType, Long> daoInstance = null;

    public PackageTypeDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<PackageType, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(PackageType.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating PackageType DAO", e);
            throw new RuntimeException("Error creating PackageType DAO", e);
        }
    }


}