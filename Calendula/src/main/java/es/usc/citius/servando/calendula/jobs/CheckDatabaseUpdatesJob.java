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

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;

import es.usc.citius.servando.calendula.drugdb.updates.DBVersionManager;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class CheckDatabaseUpdatesJob extends CalendulaJob {

    public final static String TAG = "CheckDatabaseUpdatesJob";

    private static final Integer PERIOD_DAYS = 7;

    public CheckDatabaseUpdatesJob() {
    }

    @Override
    public Duration getInterval() {
        return Duration.standardDays(PERIOD_DAYS);
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
        final SharedPreferences prefs = PreferenceUtils.instance().preferences();
        final String database = prefs.getString(PreferenceKeys.DRUGDB_CURRENT_DB, null);
        final String currentVersion = prefs.getString(PreferenceKeys.DRUGDB_VERSION, null);

        if (database != null) {
            if (currentVersion != null) {
                final String lastDBVersion = DBVersionManager.getLastDBVersion(database);
                final DateTime lastDBDate = DateTime.parse(lastDBVersion, ISODateTimeFormat.basicDate());
                final DateTime currentDBDate = DateTime.parse(currentVersion, ISODateTimeFormat.basicDate());

                if (lastDBDate.isAfter(currentDBDate)) {
                    // update is available!
                    Log.d(TAG, "onRunJob: Update found for database " + database + " (" + lastDBVersion + ")");
                }
            } else {
                Log.e(TAG, "Database is " + database + " but no version is set!");
            }
        }

        return Result.SUCCESS;
    }

}
