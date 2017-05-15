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

import org.joda.time.LocalTime;

import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by joseangel.pineiro on 10/9/14.
 */
public class Testing {

    private static final String TAG = "Testing";

    public static void test() {

        LogUtil.d(TAG, "================================================================");

        List<Routine> routines = Routine.findAll();

        for (Routine routine : routines) {
            Medicine med = routine.scheduleItems().get(0).schedule().medicine();
            LogUtil.d(TAG, "Presentation of " + med.name() + ": " + med.presentation().name());
        }

        Routine r = Routine.findByName("Breakfast");

        if (r == null) {
            // create and save routine
            r = new Routine(LocalTime.now(), "Lunch");
            r.save();
        } else if (r.time() == null) {
            r.setTime(LocalTime.now());
            r.save();
        }

        // create and sve medicine
        Medicine m = new Medicine("Paracetamol", Presentation.EFFERVESCENT);
        m.save();

        // create and sve medicine
        Medicine m2 = new Medicine("Eferalgan", Presentation.CAPSULES);
        m2.save();

        // create and save schedule
        Schedule s = new Schedule(m);
        s.save();
        // create and save items
        ScheduleItem item = new ScheduleItem(s, r, 1);
        item.save();

        DailyScheduleItem dsi = new DailyScheduleItem(item);
        dsi.save();


        for (Routine routine : routines) {
            LogUtil.d(TAG, "Routine: " + Routine.findById(routine.getId()).name() + ", " + routine.time());
            for (ScheduleItem i : routine.scheduleItems()) {
                LogUtil.d(TAG, " -- ScheduleItem: " + i.getId() + ", " + i.schedule().getId() + ", " + i.schedule().medicine().name());
            }

        }

        List<Medicine> medicines = Medicine.findAll();
        for (Medicine med : medicines) {
            LogUtil.d(TAG, "Medicine: " + med.name());
        }


        List<Schedule> schedules = Schedule.findAll();
        for (Schedule schedule : schedules) {
            LogUtil.d(TAG, "Schedule: " + schedule.medicine().name() + ", " + Arrays.toString(schedule.days()));
        }

//        for (DailyScheduleItem d : DailyScheduleItem.fromDate(DateTime.now())) {
//            LogUtil.d(TAG, "DSI: " + d.toString());
//        }
    }

}
