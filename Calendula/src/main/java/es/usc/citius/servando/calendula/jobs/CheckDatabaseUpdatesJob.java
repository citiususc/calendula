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

package es.usc.citius.servando.calendula.jobs;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.JobRequest;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import org.joda.time.Duration;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.drugdb.download.DBVersionManager;
import es.usc.citius.servando.calendula.drugdb.download.UpdateDatabaseService;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

public class CheckDatabaseUpdatesJob extends CalendulaJob {

    static final String TAG = "CheckDatabaseUpdJob";

    private static final Integer PERIOD_DAYS = 7;
    private static final String UPDATE_NOTIFICATION_TAG = "Calendula.notifications.update_notification";
    private static final int UPDATE_NOTIFICATION_ID = 0;


    public CheckDatabaseUpdatesJob() {
    }


    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public JobRequest getRequest() {
        return new JobRequest.Builder(getTag())
                .setPeriodic(Duration.standardDays(PERIOD_DAYS).getMillis())
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setPersisted(true)
                .build();
    }

    public boolean checkForUpdate(Context ctx) {
        if (DBVersionManager.checkForUpdate(ctx) != null) {
            notifyUpdate(ctx, PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, null));
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        LogUtil.d(TAG, "onRunJob: Job started");

        try {
            checkForUpdate(getContext());
        } catch (Exception e) {
            LogUtil.e(TAG, "onRunJob: ", e);
            return Result.FAILURE;
        }

        return Result.SUCCESS;
    }

    private void notifyUpdate(Context ctx, final String database) {

        Intent i = new Intent(ctx, UpdateDatabaseService.class);
        i.putExtra(UpdateDatabaseService.EXTRA_DATABASE_ID, database);
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
