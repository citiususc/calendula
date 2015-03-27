package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.persistence.Prescription;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class PrescriptionDao extends GenericDao<Prescription, Long> {

    public PrescriptionDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Prescription, Long> getConcreteDao() {
        try {
            return dbHelper.getPrescriptionsDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating routines dao", e);
        }
    }

}
