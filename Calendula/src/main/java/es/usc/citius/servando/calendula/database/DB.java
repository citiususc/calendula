package es.usc.citius.servando.calendula.database;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.misc.TransactionManager;

import java.util.concurrent.Callable;


public class DB {


    public static final String TAG = DB.class.getSimpleName();

    // Database name
    public static String DB_NAME = "calendula.db";
    // initialized flag
    public static boolean initialized = false;

    // DatabaseManeger reference
    private static DatabaseManager<DatabaseHelper> manager;
    // SQLite DB Helper
    private static DatabaseHelper db;

    // Medicines DAO
    private static MedicineDao Medicines;
    // Routines DAO
    private static RoutineDao Routines;
    // Schedules DAO
    private static ScheduleDao Schedules;
    // ScheduleItems DAO
    private static ScheduleItemDao ScheduleItems;
    // DailyScheduleItem DAO
    private static DailyScheduleItemDao DailyScheduleItems;
    // Prescriptions DAO
    private static PrescriptionDao Prescriptions;
    // HomogeneousGroups DAO
    private static HomogeneousGroupDao Groups;
    // Pickups DAO
    private static PickupInfoDao Pickups;

    /**
     * Initialize database and DAOs
     */
    public synchronized static void init(Context context) {

        if (!initialized) {
            initialized = true;
            manager = new DatabaseManager<>();
            db = manager.getHelper(context, DatabaseHelper.class);

            Medicines = new MedicineDao(db);
            Routines = new RoutineDao(db);
            Schedules = new ScheduleDao(db);
            ScheduleItems = new ScheduleItemDao(db);
            DailyScheduleItems = new DailyScheduleItemDao(db);
            Prescriptions = new PrescriptionDao(db);
            Groups = new HomogeneousGroupDao(db);
            Pickups = new PickupInfoDao(db);

            Log.v(TAG, "DB initialized " + DB.DB_NAME);
        }

    }

    /**
     * Dispose DB and DAOs
     */
    public synchronized static void dispose() {
        initialized = false;
        db.close();
        manager.releaseHelper(db);
        Log.v(TAG, "DB disposed");
    }

    public static DatabaseHelper helper() {
        return db;
    }

    public static void transaction(Callable<?> callable) {
        try {
            TransactionManager.callInTransaction(db.getConnectionSource(), callable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static MedicineDao medicines() {
        return Medicines;
    }

    public static RoutineDao routines() {
        return Routines;
    }

    public static ScheduleDao schedules() {
        return Schedules;
    }

    public static ScheduleItemDao scheduleItems() {
        return ScheduleItems;
    }

    public static DailyScheduleItemDao dailyScheduleItems() {
        return DailyScheduleItems;
    }

    public static PrescriptionDao prescriptions() {
        return Prescriptions;
    }

    public static HomogeneousGroupDao groups() {
        return Groups;
    }

    public static PickupInfoDao pickups() {
        return Pickups;
    }

    public static void dropAndCreateDatabase() {
        db.dropAndCreateAllTables();
    }
}
