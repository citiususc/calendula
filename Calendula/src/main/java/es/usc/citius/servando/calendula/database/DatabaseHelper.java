package es.usc.citius.servando.calendula.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = CalendulaApp.DB_NAME;
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 4;

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
            TableUtils.createTable(connectionSource, Medicine.class);
            // here we try inserting data in the on-create as a test
            getRoutinesDao();
            getMedicinesDao();
            getSchedulesDao();
            getScheduleItemsDao();


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
            //TableUtils.dropTable(connectionSource, Medicine.class, true);
            // after we drop the old databases, we create the new ones
            //onCreate(db, connectionSource);
            Log.d(DatabaseHelper.class.getName(), "OldVersion: " + oldVersion + ", newVersion: " + newVersion);

        } catch (Exception e) {
            Log.e(DatabaseHelper.class.getName(), "Can't upgrade databases", e);
            throw new RuntimeException(e);
        }
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

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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
