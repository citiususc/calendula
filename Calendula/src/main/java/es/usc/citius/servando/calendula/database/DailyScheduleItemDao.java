package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.joda.time.LocalTime;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class DailyScheduleItemDao extends GenericDao<DailyScheduleItem, Long> {

    public DailyScheduleItemDao(DatabaseHelper db)
    {
        super(db);
    }

    @Override
    public Dao<DailyScheduleItem, Long> getConcreteDao()
    {
        try
        {
            return dbHelper.getDailyScheduleItemsDao();
        } catch (SQLException e)
        {
            throw new RuntimeException("Error creating medicines dao", e);
        }
    }

    public DailyScheduleItem findByScheduleItem(ScheduleItem i)
    {
        return findOneBy(DailyScheduleItem.COLUMN_SCHEDULE_ITEM, i.getId());
    }

    public void removeAll()
    {
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception
            {
                for (DailyScheduleItem i : findAll())
                {
                    DB.dailyScheduleItems().remove(i);
                }
                return null;
            }
        });
    }

    public void removeAllFrom(final Schedule s)
    {
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception
            {
                for (DailyScheduleItem i : findBy(DailyScheduleItem.COLUMN_SCHEDULE, s))
                {
                    DB.dailyScheduleItems().remove(i);
                }
                return null;
            }
        });
    }

    public DailyScheduleItem findByScheduleAndTime(Schedule schedule, LocalTime time)
    {
        try
        {
            QueryBuilder<DailyScheduleItem, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(w.eq(DailyScheduleItem.COLUMN_SCHEDULE, schedule),
                w.eq(DailyScheduleItem.COLUMN_TIME, time));
            qb.setWhere(w);
            return qb.queryForFirst();
        } catch (SQLException e)
        {
            throw new RuntimeException("Error finding model", e);
        }
    }
}
