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

package es.usc.citius.servando.calendula.adherence;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 *
 */
public class AdherenceSummaryItem {

    public static final DateTimeFormatter DTF = ISODateTimeFormat.dateTimeNoMillis();
    private static final String TAG = "AdherenceSummaryItem";

    public Long scheduleId;
    public String drug;
    public String dateTime;
    public String timeTaken;
    public Float dose;
    public boolean taken;
    public boolean canceledByUser;
    public Long patient;


    public AdherenceSummaryItem(DailyScheduleItem item) {

        LogUtil.d(TAG, item.toString());

        Schedule s = item.boundToSchedule() ? item.getSchedule() : DB.schedules().findById(item.getScheduleItem().getSchedule().getId());
        ScheduleItem si = item.getScheduleItem(); // only for items not bound to schedule
        Medicine m = s.medicine();
        patient = item.getPatient().getId();
        drug = m.getCn();
        dose = item.boundToSchedule() ? s.dose() : si.getDose();
        taken = item.getTakenToday();
        canceledByUser = item.getTimeTaken() != null && !item.getTakenToday();
        timeTaken = item.getTakenToday() ? item.getDate().toDateTime(item.getTimeTaken()).toString(DTF) : null;
        dateTime = item.getDate().toDateTime(item.getTime()).toString(DTF);
        scheduleId = s.getId();
        LogUtil.d(TAG, toString());
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
