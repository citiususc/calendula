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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import es.usc.citius.servando.calendula.database.DB;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
@DatabaseTable(tableName = "ScheduleItems")
public class ScheduleItem {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCHEDULE = "Schedule";
    public static final String COLUMN_ROUTINE = "Routine";
    public static final String COLUMN_DOSE = "Dose";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_SCHEDULE, foreign = true, foreignAutoRefresh = true)
    private Schedule schedule;

    @DatabaseField(columnName = COLUMN_ROUTINE, foreign = true, foreignAutoRefresh = true)
    private Routine routine;

    @DatabaseField(columnName = COLUMN_DOSE)
    private float dose;

    public ScheduleItem() {
        super();
    }

    public ScheduleItem(Schedule schedule, Routine routine, float dose) {
        this();
        this.schedule = schedule;
        this.routine = routine;
        this.dose = dose;
    }

    public ScheduleItem(Schedule schedule, Routine routine) {
        this();
        this.schedule = schedule;
        this.routine = routine;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Routine getRoutine() {
        return routine;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public float getDose() {
        return dose;
    }

    public String displayDose() {
        int integerPart = (int) dose;
        double fraction = dose - integerPart;

        String fractionRational;
        if (fraction == 0.125)
            fractionRational = "1/8";
        else if (fraction == 0.25)
            fractionRational = "1/4";
        else if (fraction == 0.5)
            fractionRational = "1/2";
        else if (fraction == 0.75)
            fractionRational = "3/4";
        else if (fraction == 0)
            return "" + ((int) dose);
        else
            return "" + dose;
        return integerPart + "+" + fractionRational;

    }

    public void setDose(float dose) {
        this.dose = dose;
    }

    // *************************************
    // DB queries
    // *************************************

    public void save() {
        DB.scheduleItems().save(this);
    }

    public void deleteCascade() {
        DB.scheduleItems().deleteCascade(this);
    }


}
