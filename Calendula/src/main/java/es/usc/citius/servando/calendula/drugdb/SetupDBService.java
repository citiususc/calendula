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

package es.usc.citius.servando.calendula.drugdb;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.j256.ormlite.misc.TransactionManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Prescription;

/**
 * An {@link IntentService} subclass for handling asynchronous database setup tasks
 */
public class SetupDBService extends IntentService {

    private static final String CSV_SPACER = "\\|";
    public static final String TAG = "SetupDBService.class";
    public static int NOTIFICATION_ID = "SetupDBService".hashCode();

    private static final String ACTION_SETUP = "es.usc.citius.servando.calendula.persistence.medDatabases.action.SETUP";
    private static final String EXTRA_DB_PATH = "es.usc.citius.servando.calendula.persistence.medDatabases.extra.DB_PATH";
    private static final String EXTRA_DB_PREFERENCE_VALUE = "es.usc.citius.servando.calendula.persistence.medDatabases.extra.DB_PREFERENCE_VALUE";
    public static final String ACTION_COMPLETE = "es.usc.citius.servando.calendula.persistence.medDatabases.action.DONE";

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;

    public static void startSetup(Context context, String dbPath, String dbPreferenceValue) {
        context = context.getApplicationContext();
        Intent intent = new Intent(context, SetupDBService.class);
        intent.setAction(ACTION_SETUP);
        intent.putExtra(EXTRA_DB_PATH, dbPath);
        intent.putExtra(EXTRA_DB_PREFERENCE_VALUE, dbPreferenceValue);
        context.startService(intent);
    }

    public SetupDBService() {
        super("SetupDBService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SETUP.equals(action)) {
                final String dbPath = intent.getStringExtra(EXTRA_DB_PATH);
                final String dbPref = intent.getStringExtra(EXTRA_DB_PREFERENCE_VALUE);
                handleSetup(dbPath,dbPref);
            }
        }
    }

    private void handleSetup(final String dbPath, final String dbPref) {

        try {

            final PrescriptionDBMgr mgr = DBRegistry.instance().db(dbPref);

            TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {

                    DB.prescriptions().executeRaw("DELETE FROM Prescriptions;");

                    Prescription p = null;
                    Log.d(TAG, "Opening CSV db at " + dbPath);

                    InputStream csvStream = new FileInputStream(dbPath);
                    BufferedReader br = new BufferedReader(new InputStreamReader(csvStream));

                    int lines = 0;

                    while (br.readLine() != null) {
                        lines++;
                    }
                    br.close();

                    Log.d(TAG, "Show notification. Lines: " + lines);
                    showNotification(100, 0);

                    csvStream = new FileInputStream(dbPath);
                    br = new BufferedReader(new InputStreamReader(csvStream));
                    // step first line (headers)
                    br.readLine();
                    // read prescriptions and save them
                    int i = 0;
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (i % 2000 == 0) {
                            int prog = (int) (((float) i / lines) * 100);
                            Log.d(TAG, " Reading line " + i + "... (Progress: " + prog + ")");
                            updateNotification(100, prog);
                        }
                        i++;
                        p = mgr.fromCsv(line, CSV_SPACER);
                        if (p != null) {
                            // cn | id | name | dose | units | content
                            DB.prescriptions().executeRaw("INSERT INTO Prescriptions (Cn, Pid, Name, Dose, Packaging, Content, Generic, Prospect, Affectdriving) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
                                    p.cn, p.pid, p.name, p.dose, String.valueOf(p.packagingUnits), p.content, String.valueOf(p.generic), String.valueOf(p.hasProspect), String.valueOf(p.affectsDriving));
                        }
                    }
                    br.close();

                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SetupDBService.this);
                    SharedPreferences.Editor edit = settings.edit();
                    edit.putString("last_valid_database", dbPref);
                    edit.putString("prescriptions_database", dbPref);
                    edit.commit();



                    return null;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error while saving prescription data", e);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SetupDBService.this);
            SharedPreferences.Editor edit = settings.edit();
            edit.putString("last_valid_database", getString(R.string.database_none_id));
            edit.putString("prescriptions_database",getString(R.string.database_none_id));
            edit.commit();
        }

        // clear all allocated spaces
        Log.d(TAG, "Finish saving " + Prescription.count() + " prescriptions!");

        try {
            DB.prescriptions().executeRaw("VACUUM;");
        } catch (Exception e) {
            e.printStackTrace();
        }

        onComplete();
    }


    private void showNotification(int max, int prog){

        Intent activity = new Intent(this, MedicinesActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, activity, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
            .setTicker("")
            .setSmallIcon(android.R.drawable.stat_sys_download) //stat_notify_sync
            .setTicker("Setting up database")
            .setAutoCancel(false)
            .setContentIntent(pIntent)
            .setContentTitle("Setting up database")
            .setContentText("Setup in progress")
            .setProgress(max, prog, false);

        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    private void updateNotification(int max, int prog){
        mBuilder.setContentText("Setup in progress...");
        mBuilder.setProgress(max, prog, false);
        mBuilder.setContentInfo(prog+"%");
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void onComplete(){
        mBuilder.setContentTitle("Database setup complete");
        mBuilder.setContentText("Tap to add a new med");
        mBuilder.setProgress(100, 100, false);
        mBuilder.setSmallIcon(R.drawable.ic_done_white_36dp);
        mBuilder.setContentInfo("");
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
        Intent bcIntent = new Intent();
        bcIntent.setAction(ACTION_COMPLETE);
        sendBroadcast(bcIntent);

    }

}
