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

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import es.usc.citius.servando.calendula.util.PreferenceUtils;

/**
 * This class receives broadcasts sent from the Android Download Manager
 * when a database download is completed
 */
public class DBDownloadReceiver extends BroadcastReceiver {

    public static final String TAG = DBDownloadReceiver.class.getName();
    public static final String DOWNLOAD_MGR_DOWNLOAD_ID = "download_mgr_download_id";
    public static final String DOWNLOAD_MGR_DOWNLOAD_DB = "download_mgr_download_db";
    public static final String DOWNLOAD_MGR_DOWNLOAD_VERSION = "download_mgr_download_version";
    public static final String DOWNLOAD_MGR_DOWNLOAD_TYPE = "download_mgr_download_type";

    @Override
    public void onReceive(Context context, Intent intent) {

        long id = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
        SharedPreferences preferences = PreferenceUtils.instance().preferences();
        long downloadId = preferences.getLong(DOWNLOAD_MGR_DOWNLOAD_ID, -1);
        String downloadDb = preferences.getString(DOWNLOAD_MGR_DOWNLOAD_DB, null);
        String dbVersion = preferences.getString(DOWNLOAD_MGR_DOWNLOAD_VERSION, null);
        String type = preferences.getString(DOWNLOAD_MGR_DOWNLOAD_TYPE, null);

        android.support.v4.util.Pair<String, String> databaseInfo = new android.support.v4.util.Pair<>(downloadDb, dbVersion);

        if (downloadId != -1 && downloadDb != null && id == downloadId && dbVersion != null && type != null) {
            Pair<Integer, String> status = DownloadDatabaseHelper.instance().downloadStatus(id, context);
            if (status != null && status.first == DownloadManager.STATUS_SUCCESSFUL) {
                String path = status.second;
                Log.d(TAG, "onReceive: valid download " + id + ", " + path + ", " + intent.getExtras().getString(DownloadManager.COLUMN_URI));
                final DBInstallType dbInstallType = DBInstallType.valueOf(type);
                InstallDatabaseService.startSetup(context, path, databaseInfo, dbInstallType);

            } else {
                Log.d(TAG, "onReceive: invalid download " + id);
                DownloadDatabaseHelper.instance().onDownloadFailed(context);
            }
        }
        preferences.edit()
                .remove(DOWNLOAD_MGR_DOWNLOAD_ID)
                .remove(DOWNLOAD_MGR_DOWNLOAD_DB)
                .remove(DOWNLOAD_MGR_DOWNLOAD_VERSION)
                .remove(DOWNLOAD_MGR_DOWNLOAD_TYPE)
                .apply();
    }

}