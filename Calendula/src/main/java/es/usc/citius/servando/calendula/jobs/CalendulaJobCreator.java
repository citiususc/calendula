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

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class CalendulaJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case PurgeCacheJob.TAG:
                return new PurgeCacheJob();
            case CheckDatabaseUpdatesJob.TAG:
                return new CheckDatabaseUpdatesJob();
            default:
                return null;
        }
    }
}
