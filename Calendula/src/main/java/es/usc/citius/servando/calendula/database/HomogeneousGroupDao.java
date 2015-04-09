package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.persistence.HomogeneousGroup;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class HomogeneousGroupDao extends GenericDao<HomogeneousGroup, Long> {

    public HomogeneousGroupDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<HomogeneousGroup, Long> getConcreteDao() {
        try {
            return dbHelper.getHomogeneousGroupsDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating routines dao", e);
        }
    }

}
