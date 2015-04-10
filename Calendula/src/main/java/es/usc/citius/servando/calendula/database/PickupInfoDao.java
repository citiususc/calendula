package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;

import org.joda.time.LocalDate;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PickupInfo;

/**
 * Created by joseangel.pineiro on 3/26/15.
 */
public class PickupInfoDao extends GenericDao<PickupInfo, Long> {

    public PickupInfoDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<PickupInfo, Long> getConcreteDao() {
        try {
            return dbHelper.getPickupInfosDao();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating pickups dao", e);
        }
    }

    public List<PickupInfo> findByMedicine(Medicine m) {
        return findBy(PickupInfo.COLUMN_MEDICINE, m.getId());
    }

    public List<PickupInfo> findByFrom(LocalDate d) {
        return findBy(PickupInfo.COLUMN_FROM, d);
    }

    public PickupInfo findNext() {

        try {
            return dao.queryBuilder()
                    .orderBy(PickupInfo.COLUMN_FROM, true)
                    .where().eq(PickupInfo.COLUMN_TAKEN, false)
                    .queryForFirst();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }

    }
}
