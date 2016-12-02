/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.drugdb.model.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class PrescriptionDAO extends GenericDao<Prescription, Long> {

    public static final String TAG = "PrescriptionDAO";

    private Dao<Prescription, Long> daoInstance = null;

    public PrescriptionDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Prescription, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(Prescription.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating Prescription DAO", e);
            throw new RuntimeException("Error creating Prescription DAO", e);
        }
    }

    public boolean empty() {
        return count() <= 0;
    }

    /**
     * Fin prescriptions by name or cn, ordering by the position of the match
     *
     * @param match String to search by
     * @param limit maximum number of results
     * @return The list of prescriptions that match
     */
    public List<Prescription> findByNameOrCn(final String match, final int limit) {
        try {
            Log.d("Prescription", "Query by name: " + match);
            QueryBuilder<Prescription, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.or(w.like(Prescription.COLUMN_NAME, "%" + match + "%"), w.like(Prescription.COLUMN_CODE, match + "%"));
            qb.orderByRaw(" (CASE WHEN " + Prescription.COLUMN_NAME + " LIKE \"" + match + "%\" THEN 1 ELSE 2 END),name");
            qb.limit((long) limit);
            qb.setWhere(w);
            return qb.query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Prescription findByCn(final String code) {
        return findOneBy(Prescription.COLUMN_CODE, code);
    }


}