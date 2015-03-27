package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Schedule;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class ScheduleDao extends GenericDao<Schedule, Long> {


    public ScheduleDao(DatabaseHelper db) {
        super(db);
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

}
