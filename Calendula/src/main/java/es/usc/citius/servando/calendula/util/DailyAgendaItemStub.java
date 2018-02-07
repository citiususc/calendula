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

package es.usc.citius.servando.calendula.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Presentation;

/**
 * Stub for daily agenda view
 */
public class DailyAgendaItemStub {

    private static final String TAG = "DailyAgendaItemStub";

    public List<DailyAgendaItemStubElement> meds;

    public String title = "";
    public LocalTime time;
    public LocalDate date;
    public Long id = -1L;
    public Patient patient;

    public boolean isSpacer = false;
    public boolean isRoutine = true;
    public boolean hasEvents = false;
    public boolean displayable = false;
    public boolean isCurrentHour = false;

    public DailyAgendaItemStub(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    public DateTime dateTime() {
        return date.toDateTime(time);
    }

    @Override
    public String toString() {
        return "DailyAgendaItemStub{" +
                ", isRoutine=" + isRoutine +
                ", hasEvents=" + hasEvents +
                ", count=" + (meds != null ? meds.size() : 0) +
                ", title='" + title + '\'' +
                ", time=" + time.toString("kk:mm") +
                ", date=" + date.toString("dd/MM") +
                '}';
    }

    public static class DailyAgendaItemStubElement implements Comparable<DailyAgendaItemStubElement> {

        public int res;
        public double dose;
        public boolean taken;

        public String medName;
        public String minute;
        public String displayDose;
        public Long scheduleItemId = -1L;
        public Presentation presentation;

        @Override
        public int compareTo(DailyAgendaItemStubElement other) {
            int result = minute.compareTo(other.minute);
            if (result == 0) result = taken ? 0 : 1;
            if (result == 0) result = medName.compareTo(other.medName);
            return result;
        }
    }

}
