package es.usc.citius.servando.calendula.database;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.misc.TransactionManager;

import java.util.concurrent.Callable;


public class DB {

    public static final String TAG = DB.class.getSimpleName();

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

    /**
     * Initialize database and DAOs
     */
    public static void init(Context context) {
        DatabaseManager<DatabaseHelper> manager = new DatabaseManager<>();
        db = manager.getHelper(context, DatabaseHelper.class);

        Medicines = new MedicineDao(db);
        Routines = new RoutineDao(db);
        Schedules = new ScheduleDao(db);
        ScheduleItems = new ScheduleItemDao(db);
        DailyScheduleItems = new DailyScheduleItemDao(db);
        Prescriptions = new PrescriptionDao(db);
        // HomogeneusGroups
        Log.v(TAG, "DB initialized ");
    }

    /**
     * Dispose DB and DAOs
     */
    public static void dispose() {
        db.close();
        db = null;
        Medicines = null;
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
}
