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

package es.usc.citius.servando.calendula.database.migrationHelpers;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.persistence.Schedule;

/**
* Migrate local dates from ddMMYYYY to YYYYMMdd to make them sortable
 * Local dates are present on DailyScheduleItem, Schedule, and PickupInfo
*/
public class LocalDateMigrationHelper {

    private static final String TAG = "LocalDateMigration";

    @DatabaseTable(tableName = "Schedules")
    public static class ScheduleStub{
        @DatabaseField(columnName = Schedule.COLUMN_ID, generatedId = true)
        public Long id;
        @DatabaseField(columnName = Schedule.COLUMN_START, persisterClass = OldLocalDatePersister.class)
        public LocalDate start;
        // no args constructor
        public ScheduleStub(){}
    }

    @DatabaseTable(tableName = "DailyScheduleItems")
    public static class DailyScheduleItemStub{
        @DatabaseField(columnName = DailyScheduleItem.COLUMN_ID, generatedId = true)
        public Long id;
        @DatabaseField(columnName = DailyScheduleItem.COLUMN_DATE, persisterClass = OldLocalDatePersister.class)
        public LocalDate date;
        // no args constructor
        public DailyScheduleItemStub(){}
    }

    @DatabaseTable(tableName = "Pickups")
    public static class PickupInfoStub{
        @DatabaseField(columnName = PickupInfo.COLUMN_ID, generatedId = true)
        public Long id;

        @DatabaseField(columnName = PickupInfo.COLUMN_FROM, persisterClass = OldLocalDatePersister.class)
        public LocalDate from;

        @DatabaseField(columnName = PickupInfo.COLUMN_TO, persisterClass = OldLocalDatePersister.class)
        public LocalDate to;
        // no args constructor
        public PickupInfoStub(){}
    }

    public static void migrateLocalDates(DatabaseHelper helper) throws SQLException {
        // get stub DAOs
        Dao<ScheduleStub, Long> schedulesDao = helper.getDao(ScheduleStub.class);
        Dao<DailyScheduleItemStub, Long> dailyItemsDao= helper.getDao(DailyScheduleItemStub.class);
        Dao<PickupInfoStub, Long> pickupsDao = helper.getDao(PickupInfoStub.class);
        Log.d(TAG, "Migrating local dates...");
        // update Schedule table local dates
        List<ScheduleStub> scheduleStubs = schedulesDao.queryForAll();
        for (ScheduleStub s : scheduleStubs) {
            schedulesDao.update(s);
        }
        Log.d(TAG, "Schedules table: done.");
        // update DailyScheduleItem table local dates
        List<DailyScheduleItemStub> dailyStubs= dailyItemsDao.queryForAll();
        for (DailyScheduleItemStub d : dailyStubs) {
            dailyItemsDao.update(d);
        }
        Log.d(TAG, "DailyScheduleItems table: done.");
        // update PickupInfo  table local dates
        List<PickupInfoStub> pickups= pickupsDao.queryForAll();
        for (PickupInfoStub p : pickups) {
            pickupsDao.update(p);
        }
        Log.d(TAG, "PickupsInfo table: done.");
    }

    /**
     * Local date persister that reads dates in the ddMMYYYY format
     * and writes them in the YYYYMMdd format
     */
    public static class OldLocalDatePersister extends BaseDataType {

        String readFormat = "ddMMYYYY";
        String writeFormat = "YYYYMMdd";

        public OldLocalDatePersister() {
            super(SqlType.STRING, new Class<?>[]{LocalDate.class});
        }

        @Override
        public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
            return defaultStr;
        }

        @Override
        public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
            return results.getString(columnPos);
        }

        @Override
        public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
            return DateTimeFormat.forPattern(readFormat).parseLocalDate((String) sqlArg);
        }

        @Override
        public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
            return ((LocalDate) javaObject).toString(writeFormat);
        }

        private static final OldLocalDatePersister singleton = new OldLocalDatePersister();

        public static OldLocalDatePersister getSingleton() {
            return singleton;
        }
    }
}
