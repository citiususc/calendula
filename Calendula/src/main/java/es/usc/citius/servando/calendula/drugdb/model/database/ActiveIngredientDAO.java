package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ActiveIngredient;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class ActiveIngredientDAO extends GenericDao<ActiveIngredient, Long> {

    public static final String TAG = "ActiveIngredientDAO";

    private Dao<ActiveIngredient, Long> daoInstance = null;

    public ActiveIngredientDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<ActiveIngredient, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(ActiveIngredient.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating ActiveIngredient DAO", e);
            throw new RuntimeException("Error creating ActiveIngredient DAO", e);
        }
    }


}