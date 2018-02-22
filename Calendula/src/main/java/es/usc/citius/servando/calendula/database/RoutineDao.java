/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

import org.joda.time.LocalTime;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.LogUtil;

public class RoutineDao extends GenericDao<Routine, Long> {

    private static final String TAG = "RoutineDao";

    public RoutineDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Routine, Long> getConcreteDao() {
        try {
            return dbHelper.getRoutinesDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating routines dao", e);
        }
    }


    @Override
    public void fireEvent() {
        CalendulaApp.eventBus().post(PersistenceEvents.ROUTINE_EVENT);
    }

    @Override
    public List<Routine> findAll() {
        try {
            return dao.queryBuilder().orderBy(Routine.COLUMN_TIME, true).query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding models", e);
        }
    }


    public List<Routine> findAllForActivePatient(Context ctx) {
        return findAll(DB.patients().getActive(ctx));
    }

    public List<Routine> findAll(Patient p) {
        return findAll(p.getId());
    }


    public List<Routine> findAll(Long patientId) {
        try {
            return dao.queryBuilder()
                    .orderBy(Routine.COLUMN_TIME, true)
                    .where().eq(Routine.COLUMN_PATIENT, patientId)
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding models", e);
        }
    }

    public Routine findByPatientAndName(String name, Patient p) {
        try {
            QueryBuilder<Routine, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(Routine.COLUMN_NAME, name), w.eq(Routine.COLUMN_PATIENT, p));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding routine", e);
        }
    }


    public List<Routine> findInHour(int hour) {
        try {
            LocalTime time = new LocalTime(hour, 0);
            // get one hour interval [h:00, h:59:]
            String start = time.toString("kk:mm");
            String end = time.plusMinutes(59).toString("kk:mm");


            LocalTime endTime = time.plusMinutes(59);

            return queryBuilder().where()
                    .between(Routine.COLUMN_TIME, time, endTime)
                    .query();
        } catch (Exception e) {
            LogUtil.e(TAG, "Error in findInHour", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteCascade(final Routine r, boolean fireEvent) {

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Collection<ScheduleItem> items = r.getScheduleItems();
                for (ScheduleItem i : items) {
                    i.deleteCascade();
                }
                DB.routines().remove(r);
                return null;
            }
        });

        if (fireEvent) {
            fireEvent();
        }
    }
}
