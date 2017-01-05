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

import com.evernote.android.job.Job;

import org.joda.time.Duration;

/**
 * Created by alvaro.brey.vilas on 05/01/17.
 */

public abstract class CalendulaJob extends Job {

    /**
     * Interval with wich the job will repeat.
     *
     * @return the interval
     */
    public abstract Duration getInterval();

    /**
     * Identifier tag for the job. Remember it should be added in CalendulaJobCreator as well.
     *
     * @return the tag
     */
    public abstract String getTag();

    /**
     * Return <code>true</code> if the job should only be executed when the device is idle.
     *
     * @return whether the job requires idle device
     */
    public abstract boolean requiresIdle();


    /**
     * @return whether the job should persist across reboots/app restarts
     */
    public abstract boolean isPersisted();

    /**
     * If <code>true</code>, there can only be a copy of this job scheduled at a given time.
     * The first scheduled copy is the one that persists. Newer copies do not overwrite, but are
     * instead ignored.
     *
     * @return whether the job should be unique
     */
    public boolean isUnique() {
        return true;
    }

}
