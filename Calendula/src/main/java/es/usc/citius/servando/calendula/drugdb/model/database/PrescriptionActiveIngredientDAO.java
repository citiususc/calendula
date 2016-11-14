
package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionActiveIngredient;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class PrescriptionActiveIngredientDAO extends GenericDao<PrescriptionActiveIngredient, Long> {

    public static final String TAG = "PrescriptionAIDAO";

    private Dao<PrescriptionActiveIngredient, Long> daoInstance = null;

    public PrescriptionActiveIngredientDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<PrescriptionActiveIngredient, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(PrescriptionActiveIngredient.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating PrescriptionActiveIngredient DAO", e);
            throw new RuntimeException("Error creating PrescriptionActiveIngredient DAO", e);
        }
    }


}