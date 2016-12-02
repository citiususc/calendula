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

package es.usc.citius.servando.calendula.drugdb;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import es.usc.citius.servando.calendula.util.Settings;
import es.usc.citius.servando.calendula.util.SettingsKeys;

/**
 *
 */
public class DBDownloader {

    public static  String DOWNLOAD_URL = null;
    private static final String TAG = DBDownloader.class.getSimpleName();
    private static final String downloadSuffix = ".db";

    private static long downloadId = -1;

    public static void download(final Context ctx, PrescriptionDBMgr db, final DBDownloadListener l) {

        if (DOWNLOAD_URL == null)
            DOWNLOAD_URL = Settings.instance().get(SettingsKeys.DATABASE_LOCATION);

        final DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);

        if (downloadId != -1)
            manager.remove(downloadId);

        final String dbName = db.id();
        String url = DOWNLOAD_URL + dbName + downloadSuffix;
        Log.d(TAG, "Download URL: " + url);

        final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + dbName + downloadSuffix;
        File f = new File(path);

        Log.d(TAG, "Path to save: " + f.getAbsolutePath());

        if (f.exists()) {
            f.delete();
        }

        NotificationManager mNotifyManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.cancel(SetupDBService.NOTIFICATION_ID);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(db.description());
        request.setTitle(db.displayName());

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, dbName + downloadSuffix);

        if (l != null) {
            final BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent i) {
                    long id = i.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                    if (id == downloadId) {

                        if (validDownload(id, manager)) {

                            File out = new File(path);
                            Log.d(TAG, "Path: " + out.getAbsolutePath() + ", " + i.getExtras().getString(DownloadManager.COLUMN_URI));

                            if (out.exists()) {
                                l.onComplete(true, out.getAbsolutePath());
                            } else {
                                l.onComplete(false, null);
                            }
                        } else {
                            l.onComplete(false, null);
                        }
                        downloadId = -1;
                        ctx.unregisterReceiver(this);
                    }

                }
            };
            ctx.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        // get download service and enqueue file
        downloadId = manager.enqueue(request);
    }


    public interface DBDownloadListener {
        void onComplete(boolean success, String path);
    }

    private static boolean validDownload(long downloadId, DownloadManager dMgr) {
        Log.d(TAG, "Checking download status for id: " + downloadId);
        //Verify if download is a success
        Cursor c = dMgr.query(new DownloadManager.Query().setFilterById(downloadId));

        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Log.d(TAG, "File was downloading properly.");
                return true;
            } else {
                int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                Log.d(TAG, "Download not correct, status [" + status + "] reason [" + reason + "]");
                return false;
            }
        }
        return false;
    }

}
