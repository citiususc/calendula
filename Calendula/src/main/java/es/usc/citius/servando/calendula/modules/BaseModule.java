package es.usc.citius.servando.calendula.modules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.mikepenz.iconics.Iconics;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.DefaultDataGenerator;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.PatientDao;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.jobs.CalendulaJobCreator;
import es.usc.citius.servando.calendula.jobs.PurgeCacheJob;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.scheduling.AlarmIntentParams;
import es.usc.citius.servando.calendula.scheduling.AlarmReceiver;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;
import es.usc.citius.servando.calendula.util.Settings;

/**
 * Created by alvaro.brey.vilas on 30/11/16.
 */

public class BaseModule extends CalendulaModule {


    public static final String ID = "CALENDULA_BASE_MODULE";

    private static final String TAG = "BaseModule";

    /*----------*/

    SharedPreferences prefs;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    void onApplicationStartup(Context ctx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        PreferenceUtils.init(ctx);
        // initialize SQLite engine
        initializeDatabase(ctx);
        if (!prefs.getBoolean("DEFAULT_DATA_INSERTED", false)) {
            DefaultDataGenerator.fillDBWithDummyData(ctx);
            prefs.edit().putBoolean("DEFAULT_DATA_INSERTED", true).commit();
        }

        // initialize daily agenda
        DailyAgenda.instance().setupForToday(ctx, false);
        // setup alarm for daily agenda update
        setupUpdateDailyAgendaAlarm(ctx);
        //exportDatabase(this, DB_NAME, new File(Environment.getExternalStorageDirectory() + File.separator + DB_NAME));
        //forceLocale(Locale.GERMAN);
        //only required if you add a custom or generic font on your own
        Iconics.init(ctx);
        //register custom fonts like this (or also provide a font definition file)
        Iconics.registerFont(new PresentationsTypeface());

        //load settings
        try {
            Settings.instance().load(ctx);
        } catch (Exception e) {
            Log.w(TAG, "onCreate: An exception happened when loading settings file");
        }
        updatePreferences(ctx);

        //initialize job engine
        JobManager.create(ctx).addJobCreator(new CalendulaJobCreator());
        PurgeCacheJob.scheduleJob();
    }

    public void initializeDatabase(Context ctx) {
        DB.init(ctx);
        DBRegistry.init(ctx);
        try {
            if (DB.patients().countOf() == 1) {
                Patient p = DB.patients().getDefault();
                prefs.edit().putLong(PatientDao.PREFERENCE_ACTIVE_PATIENT, p.id()).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setupUpdateDailyAgendaAlarm(Context ctx) {
        // intent our receiver will receive
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        AlarmIntentParams params = AlarmIntentParams.forDailyUpdate();
        AlarmScheduler.setAlarmParams(intent, params);
        PendingIntent dailyAlarm = PendingIntent.getBroadcast(ctx, params.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new LocalTime(0, 0).toDateTimeToday().getMillis(), AlarmManager.INTERVAL_DAY, dailyAlarm);
        }
    }

    private void updatePreferences(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean dbWasEnabled = prefs.getBoolean("enable_prescriptions_db", false);
        SharedPreferences.Editor editor = prefs.edit();

        // replace old "run db preference" with the default db key (AEMPS)
        if (dbWasEnabled) {
            editor.putString("last_valid_database", DBRegistry.instance().defaultDBMgr().id())
                    .putString("prescriptions_database", DBRegistry.instance().defaultDBMgr().id());
        }
        editor.remove("enable_prescriptions_db");
        editor.commit();
    }


}
