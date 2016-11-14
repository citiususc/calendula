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

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Prescription;

/**
 * An {@link IntentService} subclass for handling asynchronous database setup tasks
 */
public class SetupDBService extends IntentService {

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
            // get a reference to  the selected dbManager
            final PrescriptionDBMgr mgr = DBRegistry.instance().db(dbPref);
            // call mgr setup in order to let it insert prescriptions data
            mgr.setup(SetupDBService.this, dbPath, new PrescriptionDBMgr.SetupProgressListener() {
                @Override
                public void onProgressUpdate(int progress) {
                    showNotification(100, progress);
                }
            });

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SetupDBService.this);
            SharedPreferences.Editor edit = settings.edit();
            edit.putString("last_valid_database", dbPref);
            edit.putString("prescriptions_database", dbPref);
            edit.commit();

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
