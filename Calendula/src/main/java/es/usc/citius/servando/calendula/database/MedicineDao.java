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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.database;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.allergies.AllergyAlertUtil;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.persistence.Schedule;

/**
 * Created by joseangel.pineiro
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

    public List<Medicine> findAllForActivePatient(Context ctx) {
        return findAll(DB.patients().getActive(ctx));
    }

    public List<Medicine> findAll(Patient p) {
        return findAll(p.id());
    }


    public List<Medicine> findAll(Long patientId) {
        try {
            return dao.queryBuilder()
                    .where().eq(Medicine.COLUMN_PATIENT, patientId)
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding models", e);
        }
    }

    @Override
    public void fireEvent() {
        CalendulaApp.eventBus().post(PersistenceEvents.MEDICINE_EVENT);
    }

    public void deleteCascade(final Medicine m, boolean fireEvent) {

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                List<Schedule> schedules = Schedule.findByMedicine(m);
                for (Schedule s : schedules) {
                    s.deleteCascade();
                }
                List<PickupInfo> pickups = DB.pickups().findByMedicine(m);
                for (PickupInfo p : pickups) {
                    DB.pickups().remove(p);
                }
                DB.medicines().remove(m);
                //remove allergy alerts for this medicine
                AllergyAlertUtil.removeAllergyAlerts(m);

                return null;
            }
        });

        if (fireEvent) {
            fireEvent();
        }

    }

    public Medicine findByGroupAndPatient(Long group, Patient p) {
        try {
            QueryBuilder<Medicine, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(Medicine.COLUMN_HG, group), w.eq(Medicine.COLUMN_PATIENT, p));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding med", e);
        }
    }

    public Medicine findByCnAndPatient(String cn, Patient p) {
        try {
            QueryBuilder<Medicine, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(Medicine.COLUMN_CN, cn), w.eq(Medicine.COLUMN_PATIENT, p));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding med", e);
        }
    }

}
