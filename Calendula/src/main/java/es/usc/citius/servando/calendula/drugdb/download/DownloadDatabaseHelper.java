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

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.net.URI;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.Settings;
import es.usc.citius.servando.calendula.util.SettingsKeys;

/**
 * Created by joseangel.pineiro on 9/2/15.
 */
public class DownloadDatabaseHelper {

    public static final String TAG = "DDDialogHelper.class";
    private static final String downloadSuffix = ".db";

    private static DownloadDatabaseHelper instance;

    private DownloadDatabaseHelper() {
    }

    public static DownloadDatabaseHelper instance() {
        if (instance == null)
            instance = new DownloadDatabaseHelper();
        return instance;
    }

    public void showDownloadDialog(final Context dialogCtx, final String database, final DownloadDatabaseDialogCallback callback) {

        Log.d(TAG, "ShowDownloadDatabase");
        final Context appContext = dialogCtx.getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(dialogCtx);
        builder.setTitle("Setup med database");
        builder.setCancelable(false);
        builder.setMessage("The database will be downloaded and configured. This may take a few seconds, but you can continue using the application in the meantime.")
                .setCancelable(false)
                .setPositiveButton("Download and setup", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(dialogCtx, "Downloading medicines info...", Toast.LENGTH_SHORT).show();
                        if (callback != null) {
                            callback.onDownloadAcceptedOrCancelled(true);
                        }
                        downloadDatabase(appContext, database);
                    }
                })
                .setNegativeButton(dialogCtx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (callback != null) {
                            callback.onDownloadAcceptedOrCancelled(false);
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onDownloadFailed(Context context) {
        InstallDatabaseService.isRunning = false;
        Toast.makeText(context, "Error downloading database :(", Toast.LENGTH_SHORT).show();
        SharedPreferences settings = PreferenceUtils.instance().preferences();
        SharedPreferences.Editor edit = settings.edit();
        edit.putString("last_valid_database", context.getString(R.string.database_none_id));
        edit.putString("prescriptions_database", context.getString(R.string.database_none_id));
        edit.commit();
        Intent bcIntent = new Intent();
        bcIntent.setAction(InstallDatabaseService.ACTION_ERROR);
        context.sendBroadcast(bcIntent);
    }

    public Pair<Integer, String> downloadStatus(long downloadId, Context context) {
        final DownloadManager dMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Log.d(TAG, "Checking download status for id: " + downloadId);
        // Verify if download was successful
        Cursor c = dMgr.query(new DownloadManager.Query().setFilterById(downloadId));
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
            String path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Log.d(TAG, "File was downloading properly. " + title);
                try {
                    // convert uri to file path
                    path = new File(new URI(path).getPath()).getAbsolutePath();
                } catch (Exception e) {
                    path = null;
                }
                return new Pair<>(status, path);
            } else {
                int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                Log.d(TAG, "Download not correct, status [" + status + "] reason [" + reason + "]  " + title);
                return new Pair<>(status, null);
            }
        }
        return null;
    }

    public boolean isDBDownloadingOrInstalling(Context context) {
        long downloadId = PreferenceUtils.instance().preferences().getLong(DBDownloadReceiver.DOWNLOAD_MGR_DOWNLOAD_ID, -1);
        Pair<Integer, String> status = downloadStatus(downloadId, context);
        Log.d(TAG, "isDBDownloadingOrInstalling: " + status + ", " + InstallDatabaseService.isRunning);
        int st = status != null ? status.first : DownloadManager.STATUS_FAILED;
        boolean iroi = InstallDatabaseService.isRunning || (st != DownloadManager.STATUS_FAILED && st != DownloadManager.STATUS_SUCCESSFUL);
        Log.d(TAG, "isDBDownloadingOrInstalling: " + iroi);
        return iroi;

    }

    private void downloadDatabase(Context ctx, final String database) {
        PrescriptionDBMgr mgr = DBRegistry.instance().db(database);
        if (mgr != null) {
            InstallDatabaseService.isRunning = true;
            NotificationManager mNotifyManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            final DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            final String downloadUrl = Settings.instance().get(SettingsKeys.DATABASE_LOCATION);
            final String dbName = mgr.id();
            final String url = downloadUrl + dbName + downloadSuffix;
            // remove previous downloads and cancel notifications
            removePreviousDownloads(dbName);
            mNotifyManager.cancel(InstallDatabaseService.NOTIFICATION_ID);
            // create the download request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription(mgr.description());
            request.setTitle(mgr.displayName());
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, dbName + downloadSuffix);
            // get download service and enqueue file
            long downloadId = manager.enqueue(request);
            // save id in preferences for later use in DBDownloadReceiver
            SharedPreferences preferences = PreferenceUtils.instance().preferences();
            preferences.edit()
                    .putLong("download_mgr_download_id", downloadId)
                    .putString("download_mgr_download_db", dbName)
                    .apply();
        } else {
            Toast.makeText(ctx, "Database not available :(", Toast.LENGTH_SHORT).show();
            onDownloadFailed(ctx);
        }
    }

    private void removePreviousDownloads(String dbName) {
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        final String path = downloads.getAbsolutePath() + "/" + dbName + downloadSuffix;
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
    }

    public interface DownloadDatabaseDialogCallback {
        void onDownloadAcceptedOrCancelled(boolean accepted);
    }

}
