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

import com.evernote.android.job.JobRequest;

import org.joda.time.Duration;

import es.usc.citius.servando.calendula.util.HtmlCacheManager;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class PurgeCacheJob extends CalendulaJob {

    private static final String TAG = "PurgeCacheJob";

    private static final Integer PERIOD_DAYS = 30;

    public PurgeCacheJob() {
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public JobRequest getRequest() {
        return new JobRequest.Builder(getTag())
                .setPeriodic(Duration.standardDays(PERIOD_DAYS).getMillis())
                .setRequiresDeviceIdle(true)
                .setPersisted(true)
                .build();
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        LogUtil.d(TAG, "onRunJob: Job started");
        Integer purged = HtmlCacheManager.getInstance().purgeCache();
        LogUtil.d(TAG, "onRunJob: Purged " + purged + " entries");
        return Result.SUCCESS;
    }

}
