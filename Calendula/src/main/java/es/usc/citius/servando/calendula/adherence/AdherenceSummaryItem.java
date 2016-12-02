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

package es.usc.citius.servando.calendula.adherence;

import android.util.Log;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * 
 */
public class AdherenceSummaryItem {

    public static final DateTimeFormatter DTF = ISODateTimeFormat.dateTimeNoMillis();

    public Long scheduleId;
    public String drug;
    public String dateTime;
    public String timeTaken;
    public Float dose;
    public boolean taken;
    public boolean canceledByUser;
    public Long patient;


    public AdherenceSummaryItem(DailyScheduleItem item) {

        Log.d("TEST", item.toString());

        Schedule s = item.boundToSchedule() ? item.schedule() : DB.schedules().findById(item.scheduleItem().schedule().getId());
        ScheduleItem si = item.scheduleItem(); // only for items not bound to schedule
        Medicine m = s.medicine();
        patient = item.patient().id();
        drug = m.cn();
        dose = item.boundToSchedule() ? s.dose() : si.dose();
        taken = item.takenToday();
        canceledByUser = item.timeTaken() != null && !item.takenToday();
        timeTaken = item.takenToday() ? item.date().toDateTime(item.timeTaken()).toString(DTF) : null;
        dateTime = item.date().toDateTime(item.time()).toString(DTF);
        scheduleId = s.getId();
        Log.d("TEST", toString());
    }

    @Override
    public String toString() {
        return "AdherenceSummaryItem{" +
                "scheduleId=" + scheduleId +
                ", drug='" + drug + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", timeTaken='" + timeTaken + '\'' +
                ", dose=" + dose +
                ", taken=" + taken +
                ", canceledByUser=" + canceledByUser +
                '}';
    }
}
