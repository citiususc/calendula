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

package es.usc.citius.servando.calendula.persistence;

import org.joda.time.LocalTime;

import java.util.Comparator;

public class ScheduleItemComparator implements Comparator<ScheduleItem> {

    @Override
    public int compare(ScheduleItem a, ScheduleItem b) {

        Routine routineA = a.getRoutine();
        Routine routineB = b.getRoutine();

        if (routineA == null) {
            return 1;
        } else if (routineB == null) {
            return -1;
        }

        if (routineA.getTime() == null && routineB.getTime() == null)
            return 0;
        else if (routineA.getTime() == null)
            return -1;
        else if (routineB.getTime() == null)
            return 1;
        else if (routineA.getTime().equals(LocalTime.MIDNIGHT))
            return 1;
        else
            return routineA.getTime().compareTo(routineB.getTime());
    }
}

