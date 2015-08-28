package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class ScheduleDao extends GenericDao<Schedule, Long> {

    public ScheduleDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<Schedule, Long> getConcreteDao()
    {
        try
        {
            return dbHelper.getSchedulesDao();
        } catch (SQLException e)
        {
            throw new RuntimeException("Error creating medicines dao", e);
        }
    }

    public List<Schedule> findByMedicine(Medicine m)
    {
        return findBy(Schedule.COLUMN_MEDICINE, m.getId());
    }

    @Override
    public void fireEvent()
    {
        CalendulaApp.eventBus().post(PersistenceEvents.SCHEDULE_EVENT);
    }

    public void deleteCascade(final Schedule s, boolean fireEvent) {
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception
            {
                for (ScheduleItem i : s.items())
                {
                    i.deleteCascade();
                }
                DB.dailyScheduleItems().removeAllFrom(s);
                DB.schedules().remove(s);
                return null;
            }
        });

        if (fireEvent)
        {
            fireEvent();
        }
    }

    public List<Schedule> findHourly()
    {
        return findBy(Schedule.COLUMN_TYPE, Schedule.SCHEDULE_TYPE_HOURLY);
    }

    public Schedule findScannedByMedicine(Medicine m) {
        try
        {
            QueryBuilder<Schedule, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(Schedule.COLUMN_MEDICINE, m),w.eq(Schedule.COLUMN_SCANNED, true));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e)
        {
            throw new RuntimeException("Error finding scanned schedule", e);
        }
    }
}
