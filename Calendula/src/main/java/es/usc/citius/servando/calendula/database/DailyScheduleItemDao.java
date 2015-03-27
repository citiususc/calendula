package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class DailyScheduleItemDao extends GenericDao<DailyScheduleItem, Long> {


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

}
