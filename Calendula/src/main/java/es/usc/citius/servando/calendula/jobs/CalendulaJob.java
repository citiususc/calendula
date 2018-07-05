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

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;


public abstract class CalendulaJob extends Job {

    /**
     * Identifier tag for the job. Remember it should be added in CalendulaJobCreator as well.
     *
     * @return the tag
     */
    public abstract String getTag();

    /**
     * If <code>true</code>, there can only be a copy of this job scheduled at a given time.
     *
     * @return whether the job should be unique
     */
    public boolean isUnique() {
        return true;
    }

    /**
     * Controls the behaviour of unique jobs.
     * If <code>true</code> and <code>isUnique</code> is <code>true</code>, new jobs will overwrite existing ones.
     * If <code>false</code>, existing jobs will persist and new ones will be discarded.
     * <p>
     * If <code>isUnique</code> is <code>false</code>, this value is ignored.
     *
     * @return the value
     */
    public boolean shouldOverwritePrevious() {
        return false;
    }


    /**
     * Returns a {@link JobRequest} with which to schedule the job.
     *
     * @return the request
     */
    public abstract JobRequest getRequest();

}
