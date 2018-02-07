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

package es.usc.citius.servando.calendula.scheduling;

import android.content.Context;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Created by joseangel.pineiro on 7/8/14.
 */
public class ScheduleUtils {

    public static String getTimesStr(int items, Context ctx) {
        switch (items) {
            case 0:
                return ctx.getString(R.string.never);
            case 1:
                return ctx.getString(R.string.once_a_day);
            case 2:
                return ctx.getString(R.string.twice_a_day);
            case 3:
                return ctx.getString(R.string.tree_times_a_day);
            case 4:
                return ctx.getString(R.string.four_times_a_day);
            default:
                return items + " " + ctx.getString(R.string.times_a_day);
        }
    }

    /**
     * Obtains the doses (Schedule Items) that are attached to a routine
     *
     * @param routine The routine
     * @return the schedule items
     */
    public static List<ScheduleItem> getRoutineScheduleItems(Routine routine, LocalDate date) {
        // iterate over routine items and filter by date
        List<ScheduleItem> doses = new ArrayList<>();
        for (ScheduleItem scheduleItem : routine.getScheduleItems()) {
            if (scheduleItem.getSchedule().enabledForDate(date)) {
                doses.add(scheduleItem);
            }
        }
        return doses;
    }

    public static String stringifyDays(boolean[] checkedDays, Context ctx) {

        String[] days = getSelectedDays(checkedDays, ctx);

        if (days.length == 7) {
            return ctx.getString(R.string.every_day);
        } else if (days.length == 0) {
            return ctx.getString(R.string.never);
        }

        String dayStr = "";
        for (int i = 0; i < days.length - 1; i++) {
            if (i > 0) {
                dayStr += ", ";
            }
            dayStr += days[i];
        }
        return dayStr + ((days.length > 1 ? " " + ctx.getString(R.string.and) + " " : "") + days[days.length - 1]);
    }

    public static String[] getSelectedDays(boolean[] days, Context ctx) {

        String[] dayNames = ctx.getResources().getStringArray(R.array.day_names);
        ArrayList<String> sdays = new ArrayList<>();
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                sdays.add(dayNames[i]);
            }
        }
        return sdays.toArray(new String[sdays.size()]);

    }
}
