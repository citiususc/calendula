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

package es.usc.citius.servando.calendula.jobs;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.drugdb.updates.DBVersionManager;
import es.usc.citius.servando.calendula.drugdb.updates.UpdateDatabaseService;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class CheckDatabaseUpdatesJob extends CalendulaJob {

    public final static String TAG = "CheckDatabaseUpdatesJob";

    private static final Integer PERIOD_DAYS = 7;
    private static final String UPDATE_NOTIFICATION_TAG = "Calendula.notifications.update_notification";
    private static final int UPDATE_NOTIFICATION_ID = 0;


    public CheckDatabaseUpdatesJob() {
    }

    @Override
    public Duration getInterval() {
        return Duration.standardDays(PERIOD_DAYS);
        //return Duration.standardMinutes(1L);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public boolean requiresIdle() {
        return false;
    }

    @Override
    public boolean isPersisted() {
        return true;
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Log.d(TAG, "onRunJob: Job started");
        final SharedPreferences prefs = PreferenceUtils.instance().preferences();
        final Context ctx = getContext();
        final String noneId = ctx.getString(R.string.database_none_id);
        final String database = prefs.getString(PreferenceKeys.DRUGDB_CURRENT_DB, noneId);
        final String currentVersion = prefs.getString(PreferenceKeys.DRUGDB_VERSION, null);

        if (!database.equals(noneId) && !database.equals(ctx.getString(R.string.database_setting_up))) {
            if (currentVersion != null) {
                final String lastDBVersion = DBVersionManager.getLastDBVersion(database);
                final DateTime lastDBDate = DateTime.parse(lastDBVersion, ISODateTimeFormat.basicDate());
                final DateTime currentDBDate = DateTime.parse(currentVersion, ISODateTimeFormat.basicDate());

                if (lastDBDate.isAfter(currentDBDate)) {
                    Log.d(TAG, "onRunJob: Update found for database " + database + " (" + lastDBVersion + ")");
                    notifyUpdate(ctx);
                } else {
                    Log.d(TAG, "onRunJob: Database is updated. ID is '" + database + "', version is '" + currentVersion + "'");
                }
            } else {
                Log.e(TAG, "Database is " + database + " but no version is set!");
            }
        } else {
            Log.d(TAG, "onRunJob: No database. No version check needed.");
        }

        return Result.SUCCESS;
    }

    private void notifyUpdate(Context ctx) {

        Intent i = new Intent(ctx, UpdateDatabaseService.class);
        PendingIntent updateIntent = PendingIntent.getService(ctx, 0, i, 0);


        NotificationManager nManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
                .setContentTitle(ctx.getString(R.string.title_database_update_available))
                .setContentText(ctx.getString(R.string.action_download_update))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(ctx.getString(R.string.action_download_update)))
                .setTicker(ctx.getString(R.string.app_name) + ctx.getString(R.string.text_database_update_available))
                .setSmallIcon(R.drawable.ic_launcher_white)
                .setLargeIcon(IconUtils.icon(ctx, CommunityMaterial.Icon.cmd_database, R.color.white, 100).toBitmap())
                .setVibrate(new long[]{0, 400})
                .setAutoCancel(true)
                .setContentIntent(updateIntent);

        nManager.notify(UPDATE_NOTIFICATION_TAG, UPDATE_NOTIFICATION_ID, builder.build());
    }

}
