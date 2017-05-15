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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class ScheduleDao extends GenericDao<Schedule, Long> {

    private static final String TAG = "ScheduleDao";

    public ScheduleDao(DatabaseHelper db) {
        super(db);
    }

    public List<Schedule> findAllForActivePatient(Context ctx) {
        return findAll(DB.patients().getActive(ctx));
    }

    public List<Schedule> findAll(Patient p) {
        return findAll(p.id());
    }


    public List<Schedule> findAll(Long patientId) {
        try {
            return dao.queryBuilder()
                    .where().eq(Schedule.COLUMN_PATIENT, patientId)
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding models", e);
        }
    }


    @Override
    public Dao<Schedule, Long> getConcreteDao() {
        try {
            return dbHelper.getSchedulesDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating medicines dao", e);
        }
    }

    public List<Schedule> findByMedicine(Medicine m) {
        return findBy(Schedule.COLUMN_MEDICINE, m.getId());
    }

    @Override
    public void fireEvent() {
        CalendulaApp.eventBus().post(PersistenceEvents.SCHEDULE_EVENT);
    }

    public void deleteCascade(final Schedule s, boolean fireEvent) {
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (ScheduleItem i : s.items()) {
                    DB.scheduleItems().deleteCascade(i);
                }
                DB.dailyScheduleItems().removeAllFrom(s);
                DB.schedules().remove(s);
                return null;
            }
        });

        if (fireEvent) {
            fireEvent();
        }
    }

    public List<Schedule> findHourly() {
        return findBy(Schedule.COLUMN_TYPE, Schedule.SCHEDULE_TYPE_HOURLY);
    }

    public Schedule findScannedByMedicine(Medicine m) {
        try {
            QueryBuilder<Schedule, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(Schedule.COLUMN_MEDICINE, m), w.eq(Schedule.COLUMN_SCANNED, true));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding scanned schedule", e);
        }
    }

    public Schedule findByMedicineAndPatient(Medicine m, Patient p) {
        try {
            QueryBuilder<Schedule, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(Schedule.COLUMN_MEDICINE, m), w.eq(Schedule.COLUMN_PATIENT, p));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding schedule", e);
        }
    }

    public List<Schedule> findByMedicineAndState(Medicine m, Schedule.ScheduleState s) {
        try {
            final PreparedQuery<Schedule> q = dao.queryBuilder().where()
                    .eq(Schedule.COLUMN_MEDICINE, m)
                    .and().eq(Schedule.COLUMN_STATE, s)
                    .and().eq(Schedule.COLUMN_PATIENT, m.patient())
                    .prepare();
            return dao.query(q);
        } catch (SQLException e) {
            LogUtil.e(TAG, "findByMedicineAndState: ", e);
            throw new RuntimeException("Error finding schedules", e);
        }
    }
}
