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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by alvaro.brey on 3/26/15.
 */
public class PatientAlertDao extends GenericDao<PatientAlert, Long> {

    private static final String TAG = "AlertDao";

    private Dao<PatientAlert, Long> daoInstance = null;

    public PatientAlertDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<PatientAlert, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(PatientAlert.class);
            return daoInstance;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating patients dao", e);
        }
    }

    public List<PatientAlert> findByMedicineAndLevel(Medicine m, int level) {
        try {
            final PreparedQuery<PatientAlert> q = dao.queryBuilder().where()
                    .eq(PatientAlert.COLUMN_MEDICINE, m)
                    .and().eq(PatientAlert.COLUMN_LEVEL, level)
                    .prepare();
            return dao.query(q);
        } catch (SQLException e) {
            LogUtil.e(TAG, "findByMedicineAndLevel: ", e);
            throw new RuntimeException("Cannot retrieve alerts: ", e);
        }
    }

    public List<PatientAlert> findByMedicineSortByLevel(Medicine m) {
        try {

            final QueryBuilder<PatientAlert, Long> qb = dao.queryBuilder();
            final Where<PatientAlert, Long> eq = qb.where().eq(PatientAlert.COLUMN_MEDICINE, m);
            qb.setWhere(eq);
            qb.orderBy(PatientAlert.COLUMN_LEVEL, false);
            return dao.query(qb.prepare());
        } catch (SQLException e) {
            LogUtil.e(TAG, "findByMedicineAndLevel: ", e);
            throw new RuntimeException("Cannot retrieve alerts: ", e);
        }
    }

    public List<PatientAlert> findByPatientMedicineAndType(Patient p, Medicine m, String className) {

        try {
            final PreparedQuery<PatientAlert> query = this.queryBuilder().where()
                    .eq(PatientAlert.COLUMN_TYPE, className).and()
                    .eq(PatientAlert.COLUMN_PATIENT, p).and()
                    .eq(PatientAlert.COLUMN_MEDICINE, m).prepare();
            return query(query);
        } catch (SQLException e) {
            LogUtil.e(TAG, "findByPatientMedicineAndType: ", e);
            return null;
        }
    }

    public List<PatientAlert> findByMedicineAndType(Medicine m, String type) {
        try {
            final PreparedQuery<PatientAlert> q = dao.queryBuilder().where()
                    .eq(PatientAlert.COLUMN_MEDICINE, m)
                    .and().eq(PatientAlert.COLUMN_TYPE, type)
                    .prepare();
            return dao.query(q);
        } catch (SQLException e) {
            LogUtil.e(TAG, "findByMedicineAndType: ", e);
            throw new RuntimeException("Cannot retrieve alerts: ", e);
        }
    }

}
