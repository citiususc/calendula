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

package es.usc.citius.servando.calendula.database;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey on 3/26/15.
 */
public class PatientAllergenDao extends GenericDao<PatientAllergen, Long> {

    public static final String TAG = "HtmlCacheDAO";

    private Dao<PatientAllergen, Long> daoInstance = null;

    public PatientAllergenDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<PatientAllergen, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(PatientAllergen.class);
            return daoInstance;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating patients dao", e);
        }
    }

    public List<PatientAllergen> findAllForActivePatient(Context ctx) {
        return findAll(DB.patients().getActive(ctx));
    }

    public List<PatientAllergen> findAllForActivePatientGroupByName(Context ctx) {
        try {
            final QueryBuilder<PatientAllergen, Long> qb = dao.queryBuilder();
            qb.where().eq(PatientAllergen.COLUMN_PATIENT, DB.patients().getActive(ctx));
            qb.groupBy(PatientAllergen.COLUMN_NAME);
            return qb.query();
        } catch (SQLException e) {
            Log.e(TAG, "findAllForActivePatientGroupByName: ", e);
            throw new RuntimeException(e);
        }
    }

    public List<PatientAllergen> findAll(Patient p) {
        return findAll(p.id());
    }


    public List<PatientAllergen> findAll(Long patientId) {
        try {
            return dao.queryBuilder()
                    .where().eq(PatientAllergen.COLUMN_PATIENT, patientId)
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding patientAllergens", e);
        }
    }


}
