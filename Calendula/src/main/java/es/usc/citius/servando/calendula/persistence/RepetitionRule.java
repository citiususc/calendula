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

package es.usc.citius.servando.calendula.persistence;


import com.google.ical.compat.jodatime.DateTimeIterator;
import com.google.ical.compat.jodatime.DateTimeIteratorFactory;
import com.google.ical.values.Frequency;
import com.google.ical.values.RRule;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joseangel.pineiro on 4/16/15.
 */
public class RepetitionRule {

    public static String DEFAULT_ICAL_VALUE = "RRULE:FREQ=DAILY;";
    private static int[] JAVA_DAY_INDEXES = new int[]{
            // 1 : sun (6), 2 : mon (0), 3 : tue (2)...
            -1, 6, 0, 1, 2, 3, 4, 5
    };
    private static Weekday[] WEEK_DAYS = new Weekday[]{
            Weekday.MO, Weekday.TU, Weekday.WE, Weekday.TH, Weekday.FR, Weekday.SA, Weekday.SU
    };
    private RRule rrule;

    // cached value for week days
    private boolean[] days;
    private String start;

    public RepetitionRule() {
        rrule = new RRule();
    }

    public RepetitionRule(String ical) {
        try {
            if (ical == null || ical.isEmpty()) {
                ical = DEFAULT_ICAL_VALUE;
            }
            rrule = new RRule(ical);
            /*if(!ical.contains("UNTIL")){
                DateValue v = new DateTimeValueImpl(1,0,0,0,0,0);
                rrule.setUntil(v);
            }*/
            //Log.d("RRule", "Creating repetition rule: " + rrule.toIcal());
        } catch (ParseException p) {
            throw new RuntimeException("Error parsing RRule", p);
        }
    }

    public String start() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public Frequency frequency() {
        return rrule.getFreq();
    }

    public int interval() {
        return rrule.getInterval();
    }

    public void setFrequency(Frequency freq) {
        rrule.setFreq(freq);
    }

    public void setInterval(int interval) {
        rrule.setInterval(interval);
    }

    public void setDays(boolean[] days) {
        List<WeekdayNum> byDay = new ArrayList<>();
        if (days != null) {
            for (int i = 0; i < days.length; i++) {
                if (days[i]) {
                    byDay.add(new WeekdayNum(0, WEEK_DAYS[i]));
                }
            }
        }
        rrule.setByDay(byDay);
        // invalidate cached value
        this.days = null;
    }

    public boolean[] days() {
        if (this.days == null) {
            this.days = new boolean[7];
            for (WeekdayNum w : rrule.getByDay()) {
                this.days[JAVA_DAY_INDEXES[w.wday.javaDayNum]] = true;
            }
        }
        return this.days;
    }


    public List<DateTime> occurrencesBetween(DateTime start, DateTime to, DateTime scheduleStart) {
        try {
            // start iterating from a date before today, to avoid having the passed time
            // as the first occurrence
            DateTime iterateFrom = scheduleStart;//(start.isAfter(s.startDateTime()) ? start : s.startDateTime()).minusHours(s.rule().interval());
            // to allow first occurrence at current hour, advance to the hour before it
            DateTime firstOccurrence = (start.isAfter(scheduleStart) ? start : scheduleStart).minusMinutes(1);

            List<DateTime> occurrences = new ArrayList<>();
            DateTimeIterator it = DateTimeIteratorFactory.createDateTimeIterator(rrule.toIcal(), iterateFrom, iterateFrom.getZone(), true);
            // advance to the start of the interval
            it.advanceTo(firstOccurrence);
            // iterate until first date out of the interval
            while (it.hasNext()) {
                DateTime n = it.next().withZone(iterateFrom.getZone());
                if (n.isBefore(to)) {
                    occurrences.add(n);
                } else {
                    break;
                }
            }
            return occurrences;
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing ical", e);
        }
    }

    public RRule iCalRule() {
        return rrule;
    }

    public String toIcal() {
        return rrule.toIcal();
    }
}
