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
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.net.URI;

import es.usc.citius.servando.calendula.BuildConfig;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.NetworkUtils;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

public class DownloadDatabaseHelper {

    private static final String TAG = "DownloadDatabaseHelper";
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

        LogUtil.d(TAG, "showDownloadDialog() called with: dialogCtx = [" + dialogCtx + "], database = [" + database + "], callback = [" + callback + "]");
        final Context appContext = dialogCtx.getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(dialogCtx);
        builder.setTitle(R.string.download_db_dialog_title);
        builder.setCancelable(false);
        builder.setMessage(R.string.download_db_dialog_message)
                .setCancelable(false)
                .setPositiveButton(R.string.db_download_and_setup, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (NetworkUtils.isNetworkAvailable(appContext)) {
                            if (callback != null) {
                                callback.onDownloadAcceptedOrCancelled(true);
                            }
                            downloadDatabase(appContext, database, DBInstallType.SETUP);
                            dialog.dismiss();
                        } else {
                            if (callback != null) {
                                callback.onDownloadAcceptedOrCancelled(false);
                            }
                            Toast.makeText(appContext, R.string.message_no_internet_error, Toast.LENGTH_SHORT).show();
                        }

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
        Toast.makeText(context, R.string.download_db_unexpected_error, Toast.LENGTH_LONG).show();
        SharedPreferences settings = PreferenceUtils.instance().preferences();
        SharedPreferences.Editor edit = settings.edit();
        edit.putString(PreferenceKeys.DRUGDB_LAST_VALID.key(), context.getString(R.string.database_none_id));
        edit.putString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), context.getString(R.string.database_none_id));
        edit.apply();
        Intent bcIntent = new Intent();
        bcIntent.setAction(InstallDatabaseService.ACTION_ERROR);
        context.sendBroadcast(bcIntent);
    }

    public Pair<Integer, String> downloadStatus(long downloadId, Context context) {
        final DownloadManager dMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        LogUtil.d(TAG, "Checking download status for id: " + downloadId);
        // Verify if download was successful
        Cursor c = dMgr.query(new DownloadManager.Query().setFilterById(downloadId));
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
            String path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                LogUtil.d(TAG, "File was downloading properly. " + title);
                try {
                    // convert uri to file path
                    path = new File(new URI(path).getPath()).getAbsolutePath();
                } catch (Exception e) {
                    path = null;
                }
                return new Pair<>(status, path);
            } else {
                int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                LogUtil.d(TAG, "Download not correct, status [" + status + "] reason [" + reason + "]  " + title);
                return new Pair<>(status, null);
            }
        }
        return null;
    }

    public boolean isDBDownloadingOrInstalling(Context context) {
        long downloadId = PreferenceUtils.getLong(PreferenceKeys.DRUGDB_DOWNLOAD_ID, -1);
        Pair<Integer, String> status = downloadStatus(downloadId, context);
        LogUtil.d(TAG, "isDBDownloadingOrInstalling: " + status + ", " + InstallDatabaseService.isRunning);
        int st = status != null ? status.first : DownloadManager.STATUS_FAILED;
        boolean iroi = InstallDatabaseService.isRunning || (st != DownloadManager.STATUS_FAILED && st != DownloadManager.STATUS_SUCCESSFUL);
        LogUtil.d(TAG, "isDBDownloadingOrInstalling: " + iroi);
        return iroi;

    }

    void downloadDatabase(Context ctx, final String database, final DBInstallType type) {
        new DownloadDatabaseTask(ctx, type).execute(database);
    }

    private void removePreviousDownloads(Context ctx, String dbName) {
        File downloads = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        final String path = downloads.getAbsolutePath() + "/" + dbName + downloadSuffix;
        File f = new File(path);
        if (f.exists()) {
            f.delete();
            LogUtil.d(TAG, "removePreviousDownloads: deleted file " + path);
        }
    }

    public interface DownloadDatabaseDialogCallback {
        void onDownloadAcceptedOrCancelled(boolean accepted);
    }

    private class DownloadDatabaseTask extends AsyncTask<String, Void, Boolean> {

        private Context ctx;
        private DBInstallType type;

        private DownloadDatabaseTask(Context ctx, DBInstallType type) {
            this.ctx = ctx;
            this.type = type;
        }


        @Override
        protected void onPostExecute(Boolean correct) {
            if (!correct) {
                onDownloadFailed(ctx);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final String database = params[0];

            PrescriptionDBMgr mgr = DBRegistry.instance().db(database);
            if (mgr != null) {
                InstallDatabaseService.isRunning = true;
                NotificationManager mNotifyManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                final DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);


                final String downloadUrl = BuildConfig.DB_DOWNLOAD_URL;
                final String dbName = mgr.id();

                try {//get version
                    final String dbVersion = DBVersionManager.getLastDBVersion(dbName);
                    final String url = ctx.getString(R.string.database_file_location, downloadUrl, dbName, dbVersion);
                    LogUtil.d(TAG, "doInBackground: Downloading database from " + url);


                    // remove previous downloads and cancel notifications
                    removePreviousDownloads(ctx, dbName);
                    mNotifyManager.cancel(InstallDatabaseService.NOTIFICATION_ID);
                    // create the download request
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setDescription(mgr.description());
                    request.setTitle(mgr.displayName());
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                    request.setVisibleInDownloadsUi(true);
                    request.setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, dbName + downloadSuffix);
                    // get download service and enqueue file
                    long downloadId = manager.enqueue(request);
                    // save id in preferences for later use in DBDownloadReceiver
                    SharedPreferences preferences = PreferenceUtils.instance().preferences();
                    preferences.edit()
                            .putLong(PreferenceKeys.DRUGDB_DOWNLOAD_ID.key(), downloadId)
                            .putString(PreferenceKeys.DRUGDB_DOWNLOAD_DB.key(), dbName)
                            .putString(PreferenceKeys.DRUGDB_DOWNLOAD_VERSION.key(), dbVersion)
                            .putString(PreferenceKeys.DRUGDB_DOWNLOAD_TYPE.key(), type.toString())
                            .apply();
                } catch (Exception e) {
                    LogUtil.e(TAG, "doInBackground: ", e);
                    return false;
                }
            } else {
                LogUtil.e(TAG, "PrescriptionDBMgr for " + database + " is null");
                return false;
            }

            return true;
        }
    }

}
