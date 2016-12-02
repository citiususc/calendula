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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalDatePersister;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalTimePersister;

/**
 * Created by castrelo
 */
@DatabaseTable(tableName = "DailyScheduleItems")
public class DailyScheduleItem {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCHEDULE_ITEM = "ScheduleItem";
    public static final String COLUMN_SCHEDULE = "Schedule";
    public static final String COLUMN_TAKEN_TODAY = "TakenToday";
    public static final String COLUMN_TIME_TAKEN = "TimeTaken";
    public static final String COLUMN_TIME = "Time";
    public static final String COLUMN_DATE = "Date";
    public static final String COLUMN_PATIENT = "Patient";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_SCHEDULE_ITEM, foreign = true, foreignAutoRefresh = true)
    private ScheduleItem scheduleItem;

    @DatabaseField(columnName = COLUMN_SCHEDULE, foreign = true, foreignAutoRefresh = true)
    private Schedule schedule;

    @DatabaseField(columnName = COLUMN_TAKEN_TODAY)
    private boolean takenToday;

    @DatabaseField(columnName = COLUMN_TIME_TAKEN, persisterClass = LocalTimePersister.class)
    private LocalTime timeTaken;

    @DatabaseField(columnName = COLUMN_TIME, persisterClass = LocalTimePersister.class)
    private LocalTime time;

    @DatabaseField(columnName = COLUMN_DATE, persisterClass = LocalDatePersister.class)
    private LocalDate date;

    @DatabaseField(columnName = COLUMN_PATIENT, foreign = true, foreignAutoRefresh = true)
    private Patient patient;

    public DailyScheduleItem() {
    }

    public DailyScheduleItem(ScheduleItem scheduleItem) {
        this.scheduleItem = scheduleItem;
        this.date = LocalDate.now();
    }

    public DailyScheduleItem(Schedule schedule, LocalTime time) {
        this.schedule = schedule;
        this.time = time;
        this.date = LocalDate.now();
    }

    public static DailyScheduleItem findById(long id) {
        return DB.dailyScheduleItems().findById(id);
    }

    public static List<DailyScheduleItem> findAll() {
        return DB.dailyScheduleItems().findAll();
    }

    public static DailyScheduleItem findByScheduleItem(ScheduleItem item) {
        return DB.dailyScheduleItems().findByScheduleItem(item);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime timeTaken() {
        return timeTaken;
    }

    public ScheduleItem scheduleItem() {
        return scheduleItem;
    }

    public Schedule schedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setTimeTaken(LocalTime date) {
        this.timeTaken = date;
    }

    public LocalTime time() {
        if (boundToSchedule())
            return time;

        return scheduleItem.routine().time();
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public boolean takenToday() {
        return takenToday;
    }

    public boolean boundToSchedule() {
        return schedule != null;
    }

    public void setTakenToday(boolean takenToday) {
        this.takenToday = takenToday;
        if (takenToday) {
            timeTaken = LocalTime.now();
        } else {
            timeTaken = null;
        }
    }

    public LocalDate date() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "DailyScheduleItem{" +
                "id=" + id +
                ", scheduleItem=" + (scheduleItem != null ? scheduleItem.getId() : "null") +
                ", schedule=" + (schedule != null ? schedule.getId() : "null") +
                ", takenToday=" + takenToday +
                ", timeTaken=" + timeTaken +
                ", time=" + time +
                ", date=" + date +
                '}';
    }

    public void save() {
        DB.dailyScheduleItems().save(this);
    }

    public Patient patient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}

