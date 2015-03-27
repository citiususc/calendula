package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.persistence.Medicine;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class MedicineDao extends GenericDao<Medicine, Long> {


    public MedicineDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Medicine, Long> getConcreteDao() {
        try {
            return dbHelper.getMedicinesDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating medicines dao", e);
        }
    }

}
