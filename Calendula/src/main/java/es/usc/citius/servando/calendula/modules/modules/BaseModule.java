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

package es.usc.citius.servando.calendula.modules.modules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.evernote.android.job.JobManager;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.Iconics;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.DefaultDataGenerator;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.PatientDao;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.download.UpdateDatabaseService;
import es.usc.citius.servando.calendula.jobs.CalendulaJob;
import es.usc.citius.servando.calendula.jobs.CalendulaJobCreator;
import es.usc.citius.servando.calendula.jobs.CalendulaJobScheduler;
import es.usc.citius.servando.calendula.jobs.CheckDatabaseUpdatesJob;
import es.usc.citius.servando.calendula.jobs.PurgeCacheJob;
import es.usc.citius.servando.calendula.modules.CalendulaModule;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.scheduling.AlarmIntentParams;
import es.usc.citius.servando.calendula.scheduling.AlarmReceiver;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;

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

    public void initializeDatabase(Context ctx) {
        DB.init(ctx);
        DBRegistry.init(ctx);
        try {
            if (DB.patients().countOf() == 1) {
                Patient p = DB.patients().getDefault();
                prefs.edit().putLong(PatientDao.PREFERENCE_ACTIVE_PATIENT, p.id()).apply();
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

    @Override
    protected void onApplicationStartup(Context ctx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        PreferenceUtils.init(ctx);
        // initialize SQLite engine
        initializeDatabase(ctx);
        if (!prefs.getBoolean("DEFAULT_DATA_INSERTED", false)) {
            DefaultDataGenerator.fillDBWithDummyData(ctx);
            prefs.edit().putBoolean("DEFAULT_DATA_INSERTED", true).apply();
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

        updatePreferences(ctx);

        //initialize job engine
        JobManager.create(ctx).addJobCreator(new CalendulaJobCreator());
        //schedule jobs
        CalendulaJob[] jobs = new CalendulaJob[]{
                new CheckDatabaseUpdatesJob(),
                new PurgeCacheJob()
        };
        CalendulaJobScheduler.scheduleJobs(jobs);
    }

    /**
     * When updating from an old version of the app (with the old DB format), checks if DB was enabled.
     * If so, triggers the download and configuration of the new DB format, using the then-default database (AEMPS).
     *
     * @param ctx the context
     */
    private void updatePreferences(final Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean dbWasEnabled = prefs.getBoolean("enable_prescriptions_db", false);

        if (dbWasEnabled) {
            new MaterialStyledDialog.Builder(ctx)
                    .setStyle(Style.HEADER_WITH_ICON)
                    .setIcon(IconUtils.icon(ctx, CommunityMaterial.Icon.cmd_database, R.color.white, 100))
                    .setHeaderColor(R.color.android_blue)
                    .withDialogAnimation(true)
                    .setTitle(R.string.title_database_update_required)
                    .setDescription(R.string.message_database_update_required)
                    .setCancelable(false)
                    .setPositiveText(ctx.getString(R.string.download))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent i = new Intent(ctx, UpdateDatabaseService.class);
                            i.putExtra(UpdateDatabaseService.EXTRA_DATABASE_ID, ctx.getString(R.string.database_aemps_id));
                            ctx.startService(i);
                        }
                    })
                    .setNegativeText(R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("enable_prescriptions_db");
        editor.apply();
    }


}
