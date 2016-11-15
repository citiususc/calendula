package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PresentationForm;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class PresentationFormDAO extends GenericDao<PresentationForm, Long> {

    public static final String TAG = "PresentationFormDAO";

    private Dao<PresentationForm, Long> daoInstance = null;

    public PresentationFormDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<PresentationForm, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(PresentationForm.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating PresentationForm DAO", e);
            throw new RuntimeException("Error creating PresentationForm DAO", e);
        }
    }


}