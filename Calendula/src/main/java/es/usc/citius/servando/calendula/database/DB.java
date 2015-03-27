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
    public static MedicineDao Medicines;
    // Routines DAO
    public static RoutineDao Routines;
    // Schedules DAO
    public static ScheduleDao Schedules;
    // ScheduleItems DAO
    public static ScheduleItemDao ScheduleItems;
    // DailyScheduleItem DAO
    public static DailyScheduleItemDao DailyScheduleItems;
    // Prescriptions DAO
    public static PrescriptionDao Prescriptions;

    /**
     * Initialize database and DAOs
     */
    public static void init(Context context) {
        DatabaseManager<DatabaseHelper> manager = new DatabaseManager<DatabaseHelper>();
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


}
