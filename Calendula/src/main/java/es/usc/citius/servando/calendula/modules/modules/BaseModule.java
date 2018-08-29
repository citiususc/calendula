/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

package es.usc.citius.servando.calendula.modules.modules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.evernote.android.job.JobManager;
import com.mikepenz.iconics.Iconics;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.DefaultDataGenerator;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.jobs.CalendulaJob;
import es.usc.citius.servando.calendula.jobs.CalendulaJobCreator;
import es.usc.citius.servando.calendula.jobs.CalendulaJobScheduler;
import es.usc.citius.servando.calendula.jobs.CheckDatabaseUpdatesJob;
import es.usc.citius.servando.calendula.jobs.PurgeCacheJob;
import es.usc.citius.servando.calendula.modules.CalendulaModule;
import es.usc.citius.servando.calendula.notifications.NotificationHelper;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.scheduling.AlarmIntentParams;
import es.usc.citius.servando.calendula.scheduling.AlarmReceiver;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;
import es.usc.citius.servando.calendula.util.security.SecuredVault;


public class BaseModule extends CalendulaModule {


    public static final String ID = "CALENDULA_BASE_MODULE";

    private static final String TAG = "BaseModule";

    /*----------*/


    @Override
    public String getId() {
        return ID;
    }

    public void initializeDatabase(Context ctx) {
        DB.init(ctx);
        DBRegistry.init(ctx);
        try {
            if (DB.patients().count() == 0) {
                final Patient defaultPatient = DB.helper().createDefaultPatient();
                DefaultDataGenerator.generateDefaultRoutines(defaultPatient, ctx);
                PreferenceUtils.edit().putLong(PreferenceKeys.PATIENTS_ACTIVE.key(), defaultPatient.getId()).apply();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "initializeDatabase: ", e);
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
        PreferenceUtils.init(ctx);

        // initialize secured vault
        SecuredVault.INSTANCE.init(ctx);

        // initialize SQLite engine
        initializeDatabase(ctx);

        // create notification channels
        NotificationHelper.createNotificationChannels(ctx);

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

        //initialize job engine
        JobManager.create(ctx).addJobCreator(new CalendulaJobCreator());
        //schedule jobs
        CalendulaJob[] jobs = new CalendulaJob[]{
                new CheckDatabaseUpdatesJob(),
                new PurgeCacheJob()
        };
        CalendulaJobScheduler.scheduleJobs(jobs);
    }

}
