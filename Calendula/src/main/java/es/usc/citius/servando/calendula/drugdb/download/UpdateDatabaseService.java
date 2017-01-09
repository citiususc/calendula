/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2017 CITIUS - USC
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

package es.usc.citius.servando.calendula.drugdb.download;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

/**
 * Created by alvaro.brey.vilas on 09/01/17.
 */

public class UpdateDatabaseService extends IntentService {

    public static final String EXTRA_DATABASE_ID = "Calendula.UpdateDatabaseService.DATABASE_NAME";
    private static final String TAG = "UpdateDatabaseService";

    public UpdateDatabaseService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with: intent = [" + intent + "]");
        final Context ctx = getApplicationContext();
        final String database = intent.getStringExtra(EXTRA_DATABASE_ID);
        if (database != null && database.equals(PreferenceUtils.instance().preferences().getString(PreferenceKeys.DRUGDB_CURRENT_DB, null)) && DBVersionManager.checkForUpdate(ctx) != null) {
            DownloadDatabaseHelper.instance().downloadDatabase(ctx, database, DBInstallType.UPDATE);
        } else {
            Log.e(TAG, "onHandleIntent: no database id provided or database has changed since update notification");
        }
    }

}
