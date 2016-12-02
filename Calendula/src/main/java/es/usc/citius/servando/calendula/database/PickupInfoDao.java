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

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
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

    public List<PickupInfo> findByFrom(LocalDate d, boolean includeTaken) {

        try {

            Where<PickupInfo, Long> where = dao.queryBuilder().where();
            where.eq(PickupInfo.COLUMN_FROM, d);
            if (!includeTaken) {
                where.and();
                where.eq(PickupInfo.COLUMN_TAKEN, false);
            }
            return where.query();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }

        //return findBy(PickupInfo.COLUMN_FROM, d);
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

    public PickupInfo exists(PickupInfo pickupInfo) {
        try {
            QueryBuilder<PickupInfo, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.and(
                    w.eq(PickupInfo.COLUMN_MEDICINE, pickupInfo.medicine()),
                    w.eq(PickupInfo.COLUMN_FROM, pickupInfo.from()),
                    w.eq(PickupInfo.COLUMN_TO, pickupInfo.to())
            );
            qb.setWhere(w);
            return qb.queryForFirst();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding scanned schedule", e);
        }
    }

    public List<PickupInfo> findByPatient(Patient p) {
        try {

            QueryBuilder<Medicine, Long> mqb = DB.medicines().queryBuilder();
            mqb.where().eq(Medicine.COLUMN_PATIENT, p);

            QueryBuilder<PickupInfo, Long> qb = dao.queryBuilder();
            qb.leftJoin(mqb);


            return qb.query();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding scanned pickups", e);
        }
    }

    public void removeByMed(Medicine med) {
        try {
            DeleteBuilder<PickupInfo, Long> qb = dao.deleteBuilder();
            qb.setWhere(qb.where().eq(PickupInfo.COLUMN_MEDICINE, med));
            qb.delete();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting pickups by medicine", e);
        }
    }
}
