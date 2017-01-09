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

package es.usc.citius.servando.calendula.drugdb.download;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;
import android.util.Log;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.util.PreferenceKeys;

/**
 * An {@link IntentService} subclass for handling asynchronous database setup tasks
 */
public class InstallDatabaseService extends IntentService {

    public static final String TAG = "InstallDatabaseService";
    public static final String ACTION_COMPLETE = "calendula.persistence.medDatabases.action.DONE";
    public static final String ACTION_ERROR = "calendula.persistence.medDatabases.action.ERROR";
    private static final String ACTION_SETUP = "calendula.persistence.medDatabases.action.SETUP";
    private static final String ACTION_UPDATE = "calendula.persistence.medDatabases.action.UPDATE";
    private static final String EXTRA_DB_PATH = "calendula.persistence.medDatabases.extra.DB_PATH";
    private static final String EXTRA_DB_PREF_VALUE = "calendula.persistence.medDatabases.extra.DB_PREF_VALUE";
    private static final String EXTRA_DB_VERSION = "calendula.persistence.medDatabases.extra.DB_VERSION";
    public static int NOTIFICATION_ID = "InstallDatabaseService".hashCode();
    public static boolean isRunning = false;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;

    public InstallDatabaseService() {
        super("SetupDBService");
    }

    public static void startSetup(Context context, String dbPath, Pair<String, String> databaseInfo, DBInstallType type) {
        context = context.getApplicationContext();
        Intent intent = new Intent(context, InstallDatabaseService.class);
        switch (type) {
            case SETUP:
                intent.setAction(ACTION_SETUP);
                break;
            case UPDATE:
                intent.setAction(ACTION_UPDATE);
                break;
        }
        intent.putExtra(EXTRA_DB_PATH, dbPath);
        intent.putExtra(EXTRA_DB_PREF_VALUE, databaseInfo.first);
        intent.putExtra(EXTRA_DB_VERSION, databaseInfo.second);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SETUP.equals(action) || ACTION_UPDATE.equals(action)) {
                final String dbPath = intent.getStringExtra(EXTRA_DB_PATH);
                final String dbPref = intent.getStringExtra(EXTRA_DB_PREF_VALUE);
                final String dbVersion = intent.getStringExtra(EXTRA_DB_VERSION);
                handleSetup(dbPath, dbPref, dbVersion);
                if (ACTION_UPDATE.equals(action)) {
                    checkForInvalidData();
                }
            }
        }
    }

    private void checkForInvalidData() {
        Log.d(TAG, "checkForInvalidData() called");
        // TODO: 09/01/17
    }

    private void handleSetup(final String dbPath, final String dbPref, final String dbVersion) {
        try {
            isRunning = true;
            // get a reference to  the selected dbManager
            final PrescriptionDBMgr mgr = DBRegistry.instance().db(dbPref);
            // call mgr setup in order to let it insert prescriptions data
            mgr.setup(InstallDatabaseService.this, dbPath, new PrescriptionDBMgr.SetupProgressListener() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.d(TAG, "Setting up db " + progress + "%");
                    showNotification(100, progress);
                }
            });

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(InstallDatabaseService.this);
            SharedPreferences.Editor edit = settings.edit();
            edit.putString(PreferenceKeys.DRUGDB_LAST_VALID, dbPref);
            edit.putString(PreferenceKeys.DRUGDB_CURRENT_DB, dbPref);
            edit.putString(PreferenceKeys.DRUGDB_VERSION, dbVersion);
            edit.apply();
            Log.d(TAG, dbPref + "-" + dbVersion + ": Finished saving " + DB.drugDB().prescriptions().count() + " prescriptions!");
            onComplete();
        } catch (Exception e) {
            Log.e(TAG, "Error while saving prescription data", e);
            DownloadDatabaseHelper.instance().onDownloadFailed(this);
            onFailure();
        }

        try {
            DB.drugDB().prescriptions().executeRaw("VACUUM;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showNotification(int max, int prog) {

        Intent activity = new Intent(this, MedicinesActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, activity, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
                .setTicker("")
                .setSmallIcon(android.R.drawable.stat_sys_download) //stat_notify_sync
                .setTicker(getString(R.string.install_db_notification_ticker))
                .setAutoCancel(false)
                .setContentIntent(pIntent)
                .setContentTitle(getString(R.string.install_db_notification_title))
                .setContentText(getString(R.string.install_db_notification_content))
                .setProgress(max, prog, false);

        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void onComplete() {
        isRunning = false;
        mBuilder.setContentTitle(getString(R.string.install_db_notification_oncomplete));
        mBuilder.setContentText(getString(R.string.install_db_notification_oncomplete_text));
        mBuilder.setAutoCancel(true);
        mBuilder.setProgress(100, 100, false);
        mBuilder.setSmallIcon(R.drawable.ic_done_white_36dp);
        mBuilder.setContentInfo("");

        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
        Intent bcIntent = new Intent();
        bcIntent.setAction(ACTION_COMPLETE);
        sendBroadcast(bcIntent);
    }

    private void onFailure() {
        isRunning = false;
        mBuilder.setContentTitle(getString(R.string.install_db_notification_onfailure));
        mBuilder.setContentText(getString(R.string.install_db_notification_onfailure_content));
        mBuilder.setAutoCancel(true);
        mBuilder.setProgress(100, 100, false);
        mBuilder.setSmallIcon(R.drawable.ic_clear_search_holo_light);
        mBuilder.setContentInfo("");
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
