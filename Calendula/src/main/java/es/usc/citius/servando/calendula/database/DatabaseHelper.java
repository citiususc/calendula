package es.usc.citius.servando.calendula.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.joda.time.LocalDate;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.RepetitionRule;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final String TAG = "DatabaseHelper";

    // List of persisted classes to simplify table creation
    public Class<?>[] persistedClasses = new Class<?>[]{
            Routine.class,
            Medicine.class,
            Schedule.class,
            ScheduleItem.class,
            DailyScheduleItem.class,
            Prescription.class,
            // v5
            HomogeneousGroup.class,
            // v6
            PickupInfo.class
    };

    // name of the database file for our application
    private static final String DATABASE_NAME = DB.DB_NAME;
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 7;

    // the DAO object we use to access the Medicines table
    private Dao<Medicine, Long> medicinesDao = null;
    // the DAO object we use to access the Routines table
    private Dao<Routine, Long> routinesDao = null;
    // the DAO object we use to access the Schedules table
    private Dao<Schedule, Long> schedulesDao = null;
    // the DAO object we use to access the ScheduleItems table
    private Dao<ScheduleItem, Long> scheduleItemsDao = null;
    // the DAO object we use to access the DailyScheduleItems table
    private Dao<DailyScheduleItem, Long> dailyScheduleItemsDao = null;
    // the DAO object we use to access the DailyScheduleItems table
    private Dao<Prescription, Long> prescriptionsDao = null;
    // the DAO object we use to access the HomogeneousGroups table
    private Dao<HomogeneousGroup, Long> homogeneousGroupsDao = null;
    // the DAO object we use to access the pcikupInfo table
    private Dao<PickupInfo, Long> pickupInfoDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");

            for (Class<?> c : persistedClasses) {
                Log.d(TAG, "Creating table for " + c.getSimpleName());
                TableUtils.createTable(connectionSource, c);
            }

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            Log.d(DatabaseHelper.class.getName(), "OldVersion: " + oldVersion + ", newVersion: " + newVersion);

            if (oldVersion < 6)
            {
                oldVersion = 6;
            }

            switch (oldVersion + 1)
            {

                case 7:
                    // migrate to iCal
                    migrateToICal();
                
                // TODO Create HGroup and PKInfo tables
                  
            }


        } catch (Exception e) {
            Log.e(DatabaseHelper.class.getName(), "Can't upgrade databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that migrate schedules to the iCal format
     */
    private void migrateToICal() throws SQLException {

        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Rrule TEXT;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Start TEXT;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Starttime TEXT;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Dose REAL;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Type INTEGER;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Cycle TEXT;");

        getDailyScheduleItemsDao().executeRaw(
            "ALTER TABLE DailyScheduleItems ADD COLUMN Schedule INTEGER;");
        getDailyScheduleItemsDao().executeRaw(
            "ALTER TABLE DailyScheduleItems ADD COLUMN Time TEXT;");

        // update schedules
        TransactionManager.callInTransaction(getConnectionSource(), new Callable<Object>() {
          @Override
          public Void call() throws Exception {
            // iterate over schedules and replace days[] with rrule
            List<Schedule> schedules = getSchedulesDao().queryForAll();
            Log.d(TAG, "Upgrade " + schedules.size() + " schedules");
            for (Schedule s : schedules) {
              if (s.rule() == null) {
                s.setRepetition(new RepetitionRule(RepetitionRule.DEFAULT_ICAL_VALUE));
              }
              s.setDays(s.getLegacyDays());

              if (s.allDaysSelected()) {
                s.setType(Schedule.SCHEDULE_TYPE_EVERYDAY);
              } else {
                s.setType(Schedule.SCHEDULE_TYPE_SOMEDAYS);
              }
              s.setStart(LocalDate.now());
              s.save();
            }

            return null;
          }
        });


    }

    /**
     * Returns the Database Access Object (DAO) for our Medicines class. It will create it or just give the cached
     * value.
     */
    public Dao<Medicine, Long> getMedicinesDao() throws SQLException {
        if (medicinesDao == null) {
            medicinesDao = getDao(Medicine.class);
        }
        return medicinesDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our Routines class. It will create it or just give the cached
     * value.
     */
    public Dao<Routine, Long> getRoutinesDao() throws SQLException {
        if (routinesDao == null) {
            routinesDao = getDao(Routine.class);
        }
        return routinesDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our Schedules class. It will create it or just give the cached
     * value.
     */
    public Dao<Schedule, Long> getSchedulesDao() throws SQLException {
        if (schedulesDao == null) {
            schedulesDao = getDao(Schedule.class);
        }
        return schedulesDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our ScheduleItem class. It will create it or just give the cached
     * value.
     */
    public Dao<ScheduleItem, Long> getScheduleItemsDao() throws SQLException {
        if (scheduleItemsDao == null) {
            scheduleItemsDao = getDao(ScheduleItem.class);
        }
        return scheduleItemsDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our DailyScheduleItem class. It will create it or just give the cached
     * value.
     */
    public Dao<DailyScheduleItem, Long> getDailyScheduleItemsDao() throws SQLException {
        if (dailyScheduleItemsDao == null) {
            dailyScheduleItemsDao = getDao(DailyScheduleItem.class);
        }
        return dailyScheduleItemsDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our DailyScheduleItem class. It will create it or just give the cached
     * value.
     */
    public Dao<Prescription, Long> getPrescriptionsDao() throws SQLException {
        if (prescriptionsDao == null) {
            prescriptionsDao = getDao(Prescription.class);
        }
        return prescriptionsDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our HomogeneousGroup class. It will create it or just give the cached
     * value.
     */
    public Dao<HomogeneousGroup, Long> getHomogeneousGroupsDao() throws SQLException {
        if (homogeneousGroupsDao == null) {
            homogeneousGroupsDao = getDao(HomogeneousGroup.class);
        }
        return homogeneousGroupsDao;
    }


    /**
     * Returns the Database Access Object (DAO) for our PickupInfo class. It will create it or just give the cached
     * value.
     */
    public Dao<PickupInfo, Long> getPickupInfosDao() throws SQLException {
        if (pickupInfoDao == null) {
            pickupInfoDao = getDao(PickupInfo.class);
        }
        return pickupInfoDao;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void dropAndCreateAllTables() {

        Log.i(DatabaseHelper.class.getName(), "Dropping all tables...");
        for (Class<?> c : persistedClasses) {
            Log.d(TAG, "Dropping table " + c.getSimpleName());
            try {
                TableUtils.dropTable(connectionSource, c, true);
            } catch (SQLException e) {
                // ignore
                Log.e(TAG, "Erro dropping table " + c.getSimpleName());
            }

        }

        try {

            Log.i(DatabaseHelper.class.getName(), "Creating tables...");
            for (Class<?> c : persistedClasses) {
                Log.d(TAG, "Creating table " + c.getSimpleName());
                TableUtils.createTable(connectionSource, c);
            }

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't recreate database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        medicinesDao = null;
    }

}
