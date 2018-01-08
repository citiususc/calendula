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
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class DailyScheduleItemDao extends GenericDao<DailyScheduleItem, Long> {


    private static final String TAG = "DailyScheduleItemDao";

    public DailyScheduleItemDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<DailyScheduleItem, Long> getConcreteDao() {
        try {
            return dbHelper.getDailyScheduleItemsDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating medicines dao", e);
        }
    }

    public DailyScheduleItem findByScheduleItem(ScheduleItem i) {
        return findOneBy(DailyScheduleItem.COLUMN_SCHEDULE_ITEM, i.getId());
    }

    public List<DailyScheduleItem> findAllByScheduleItem(ScheduleItem i) {
        return findBy(DailyScheduleItem.COLUMN_SCHEDULE_ITEM, i.getId());
    }

    public void removeAll() {
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (DailyScheduleItem i : findAll()) {
                    DB.dailyScheduleItems().remove(i);
                }
                return null;
            }
        });
    }

    public void removeAllFrom(final Schedule s) {
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (DailyScheduleItem i : findBy(DailyScheduleItem.COLUMN_SCHEDULE, s)) {
                    DB.dailyScheduleItems().remove(i);
                }
                return null;
            }
        });
    }

    public void removeAllFrom(final ScheduleItem scheduleItem) {
        final DeleteBuilder<DailyScheduleItem, Long> db = dao.deleteBuilder();
        try {
            db.setWhere(db.where().eq(DailyScheduleItem.COLUMN_SCHEDULE_ITEM, scheduleItem));
            db.delete();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting items", e);
        }
    }

    public int removeOlderThan(LocalDate date) {
        try {
            DeleteBuilder<DailyScheduleItem, Long> qb = dao.deleteBuilder();
            qb.setWhere(qb.where().lt(DailyScheduleItem.COLUMN_DATE, date));
            return qb.delete();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public int removeBeyond(LocalDate date) {
        try {
            DeleteBuilder<DailyScheduleItem, Long> qb = dao.deleteBuilder();
            qb.setWhere(qb.where().gt(DailyScheduleItem.COLUMN_DATE, date));
            return qb.delete();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public DailyScheduleItem findByScheduleAndTime(Schedule schedule, LocalTime time) {
        try {
            QueryBuilder<DailyScheduleItem, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(DailyScheduleItem.COLUMN_SCHEDULE, schedule),
                    w.eq(DailyScheduleItem.COLUMN_TIME, time));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }


    public DailyScheduleItem findByScheduleItemAndDate(ScheduleItem item, LocalDate date) {
        try {
            QueryBuilder<DailyScheduleItem, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(DailyScheduleItem.COLUMN_SCHEDULE_ITEM, item),
                    w.eq(DailyScheduleItem.COLUMN_DATE, date));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public List<DailyScheduleItem> findByPatientAndDate(Patient p, LocalDate date) {
        try {
            QueryBuilder<DailyScheduleItem, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.eq(DailyScheduleItem.COLUMN_DATE, date);
            qb.setWhere(w);
            return qb.query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public DailyScheduleItem findBy(Schedule schedule, LocalDate date, LocalTime time) {
        try {
            QueryBuilder<DailyScheduleItem, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(DailyScheduleItem.COLUMN_SCHEDULE, schedule),
                    w.eq(DailyScheduleItem.COLUMN_TIME, time),
                    w.eq(DailyScheduleItem.COLUMN_DATE, date));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }


    public List<DailyScheduleItem> findBySchedule(Schedule schedule) {
        try {
            QueryBuilder<DailyScheduleItem, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.eq(DailyScheduleItem.COLUMN_SCHEDULE, schedule);
            qb.setWhere(w);
            return qb.query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public boolean isDatePresent(LocalDate date) {
        try {
            QueryBuilder<DailyScheduleItem, Long> qb = dao.queryBuilder();
            qb.setWhere(qb.where().eq(DailyScheduleItem.COLUMN_DATE, date));
            return qb.countOf() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public void saveAndUpdateStock(DailyScheduleItem model, boolean fireEvent) {
        // get original value
        DailyScheduleItem original = findById(model.getId());
        // ensure checked status has changed
        boolean updateStock = original.getTakenToday() != model.getTakenToday();

        if (updateStock) {
            Schedule s = model.boundToSchedule() ? model.getSchedule() : model.getScheduleItem().getSchedule();
            DB.schedules().refresh(s);
            Medicine m = s.medicine();
            DB.medicines().refresh(m);

            if (m.stockManagementEnabled()) {
                try {
                    float amount;
                    if (s.repeatsHourly()) {
                        amount = model.getTakenToday() ? -s.dose() : s.dose();
                    } else {
                        ScheduleItem si = model.getScheduleItem();
                        amount = model.getTakenToday() ? -si.getDose() : si.getDose();
                    }
                    m.setStock(m.getStock() + amount);
                    DB.medicines().save(m);

                    if (fireEvent) {
                        DB.medicines().fireEvent();
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, "An error occurred updating stock", e);
                }
            }
        }
        // finally save model
        save(model);
    }
}
