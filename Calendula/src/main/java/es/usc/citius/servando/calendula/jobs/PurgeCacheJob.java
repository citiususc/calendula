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

package es.usc.citius.servando.calendula.jobs;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import org.joda.time.Duration;

import es.usc.citius.servando.calendula.util.HtmlCacheManager;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class PurgeCacheJob extends Job {

    public final static String TAG = "PurgeCacheJob";

    private static final Integer PERIOD_DAYS = 30;

    PurgeCacheJob() {
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Log.d(TAG, "onRunJob: Job started");
        Integer purged = HtmlCacheManager.getInstance().purgeCache();
        Log.d(TAG, "onRunJob: Purged " + purged + " entries");
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        // if there's already exactly one purge job running, leave it be
        int jobs = JobManager.instance().getAllJobsForTag(PurgeCacheJob.TAG).size();
        int requests = JobManager.instance().getAllJobRequestsForTag(PurgeCacheJob.TAG).size();

        Log.d(TAG, "scheduleJob: There are " + jobs + " running and " + requests + " requests already");
        if (jobs + requests != 1) {
            // if not, cancel all jobs just to be safe:
            Log.v(TAG, "Removing duplicate jobs/requests");
            JobManager.instance().cancelAllForTag(PurgeCacheJob.TAG);
            //and create one
            Log.v(TAG, "Scheduling new job");
            new JobRequest.Builder(PurgeCacheJob.TAG)
                    .setPeriodic(Duration.standardDays(PERIOD_DAYS).getMillis())
                    .setRequiresDeviceIdle(true)
                    .setPersisted(true)
                    .build()
                    .schedule();
        }
    }

}
