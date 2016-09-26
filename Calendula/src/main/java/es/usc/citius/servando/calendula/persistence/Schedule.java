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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.persistence;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrence;
import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrenceFormatter;
import com.google.ical.values.DateValue;
import com.google.ical.values.Frequency;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.typeSerializers.BooleanArrayPersister;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalDatePersister;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalTimePersister;
import es.usc.citius.servando.calendula.persistence.typeSerializers.RRulePersister;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;
import es.usc.citius.servando.calendula.util.ScheduleHelper;


/**
 * Created by joseangel.pineiro
 */
@DatabaseTable(tableName = "Schedules")
public class Schedule {

    public static final int SCHEDULE_TYPE_EVERYDAY = 0; // DEFAULT
    public static final int SCHEDULE_TYPE_SOMEDAYS = 1;
    public static final int SCHEDULE_TYPE_INTERVAL = 2;

    public static final int SCHEDULE_TYPE_HOURLY = 4;
    public static final int SCHEDULE_TYPE_CYCLE = 5;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MEDICINE = "Medicine";
    public static final String COLUMN_DAYS = "Days";
    public static final String COLUMN_RRULE = "Rrule";
    public static final String COLUMN_START = "Start";
    public static final String COLUMN_START_TIME = "Starttime";
    public static final String COLUMN_DOSE = "Dose";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_CYCLE = "Cycle";

    public static final String COLUMN_SCANNED = "Scanned";

    public static final String COLUMN_PATIENT = "Patient";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_MEDICINE, foreign = true, foreignAutoRefresh = true)
    private Medicine medicine;

    @DatabaseField(columnName = COLUMN_DAYS, persisterClass = BooleanArrayPersister.class)
    private boolean[] days = noWeekDays();

    @DatabaseField(columnName = COLUMN_RRULE, persisterClass = RRulePersister.class)
    private RepetitionRule rrule;

    @DatabaseField(columnName = COLUMN_START, persisterClass = LocalDatePersister.class)
    private LocalDate start;

    @DatabaseField(columnName = COLUMN_START_TIME, persisterClass = LocalTimePersister.class)
    private LocalTime startTime;

    @DatabaseField(columnName = COLUMN_DOSE)
    private float dose = 1f;

    @DatabaseField(columnName = COLUMN_TYPE)
    private int type = SCHEDULE_TYPE_EVERYDAY;

    @DatabaseField(columnName = COLUMN_CYCLE)
    private String cycle;

    @DatabaseField(columnName = COLUMN_SCANNED)
    private boolean scanned;

    @DatabaseField(columnName = COLUMN_PATIENT, foreign = true, foreignAutoRefresh = true)
    private Patient patient;

    public RepetitionRule rule()
    {
        return rrule;
    }

    public void setRepetition(RepetitionRule rrule)
    {
        this.rrule = rrule;
    }

    public int type()
    {
        return type;
    }

    public void setType(int type)
    {
        if (type < 0 || type > 5)
        {
            throw new RuntimeException("Invalid schedule type");
        }
        this.type = type;
    }

    public Schedule()
    {
        rrule = new RepetitionRule(null);
    }

    public Schedule(Medicine medicine)
    {
        this.medicine = medicine;
    }

    public Schedule(Medicine medicine, boolean[] days) {
        this.medicine = medicine;
        setDays(days);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ScheduleItem> items() {
        return DB.scheduleItems().findBySchedule(this);
    }

    public List<DateTime> hourlyItemsToday()
    {
        DateTime today = DateTime.now().withTimeAtStartOfDay();
        // get schedule occurrences for the current day
        return rrule.occurrencesBetween(today, today.plusDays(1), startDateTime());
    }

    public List<DateTime> hourlyItemsAt(DateTime d)
    {
        DateTime date = d.withTimeAtStartOfDay();
        // get schedule occurrences for the current day
        return rrule.occurrencesBetween(date, date.plusDays(1), startDateTime());
    }

    public Medicine medicine()
    {
        return medicine;
    }

    public void setMedicine(Medicine medicine)
    {
        this.medicine = medicine;
    }

    public boolean[] days()
    {
        return rrule.days();
    }

    public void setDays(boolean[] days)
    {
        rrule.setDays(days);
    }

    public LocalDate start() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public float dose() {
        return dose;
    }

    public Patient patient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    // *************************************
    // DB queries
    // *************************************

    public static List<Schedule> findAll()
    {
        return DB.schedules().findAll();
    }

    public static List<Schedule> findByMedicine(Medicine med)
    {
        return DB.schedules().findByMedicine(med);
    }

    public static Schedule findById(long id)
    {
        return DB.schedules().findById(id);
    }
    

    public void setDose(float dose) {
        this.dose = dose;
    }

    public boolean enabledForDate(LocalDate date) {

        if (type == SCHEDULE_TYPE_CYCLE) {
            return cycleEnabledForDate(date);
        } else {
            DateTime dt = date.toDateTimeAtStartOfDay();
            List<DateTime> occurrences = rrule.occurrencesBetween(dt, dt.plusDays(1), startDateTime());
            return occurrences.size() > 0;
        }
    }

    private boolean cycleEnabledForDate(LocalDate date) {
        return ScheduleHelper.cycleEnabledForDate(date, start, getCycleDays(), getCycleRest());
    }


    public String toReadableString(Context ctx)
    {

        if (rule().frequency() == Frequency.HOURLY)
        {
            return ctx.getString(R.string.repeat_every_tostr, rule().interval(),
                    ctx.getString(R.string.hours));
        } else if (type == SCHEDULE_TYPE_CYCLE)
        {
            return getCycleDays() + " + " + getCycleRest();
        } else if (type == SCHEDULE_TYPE_SOMEDAYS)
        {
            return ScheduleUtils.stringifyDays(days(),ctx);
        } else
        {
            String ical = rrule.toIcal();

            EventRecurrence e = new EventRecurrence();
            Time t;
            if (start != null)
            {
                t = new Time();
                t.set(start.getDayOfWeek(), start.getMonthOfYear(), start.getYear());
            } else
            {
                t = new Time();
                t.setToNow();
                t.normalize(true);
                e.setStartDate(t);
            }

            if (ical != null) e.parse(ical.replace("RRULE:", ""));

            return EventRecurrenceFormatter.getRepeatString(ctx, ctx.getResources(), e, false);
        }
    }

    public void save() {
        DB.schedules().save(this);
    }

    public void deleteCascade() {
        DB.schedules().deleteCascade(this, false);
    }

    public boolean[] getLegacyDays() {
        return days;
    }

    public boolean allDaysSelected() {
        for (boolean d : days())
            if (!d) return false;
        return true;
    }

    public boolean repeatsHourly() {
        return type == SCHEDULE_TYPE_HOURLY;
    }

    public int dayCount() {
        int count = 0;
        for (boolean d : days())
            if (d) count += 1;
        return count;
    }

    public void toggleSelectedDay(int i) {
        boolean[] d = days();
        d[i] = !d[i];
        rrule.setDays(d);
        Log.d("Schedule", "Days: " + Arrays.toString(days()));
    }
    //    final int[] byHour = rule().iCalRule().getByHour();
    //    if (byHour != null && byHour.length == 1)
    //    {
    //        final int[] byMinute = rule().iCalRule().getByMinute();
    //        if (byMinute != null && byMinute.length == 1)
    //        {
    //            return new LocalTime(byHour[0], byMinute[0]);
    //        }
    //    }
    //    return null;
    //}

    public LocalDate end() {
        DateValue v = rrule.iCalRule().getUntil();
        return v != null ? new LocalDate(v.year(), v.month(), v.day()) : null;
    }

    public LocalTime startTime() {
        return startTime;
    }

    public void setStartTime(LocalTime t) {
        startTime = t;

        //rule().iCalRule().setByHour(new int[] { t.getHourOfDay() });
        //rule().iCalRule().setByMinute(new int[] { t.getMinuteOfHour() });
    }

    public DateValue until() {
        return rrule.iCalRule().getUntil();
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", medicine=" + medicine +
                ", rrule=" + rrule.toIcal() +
                ", start=" + start +
                ", dose=" + dose +
                ", type=" + type +
                '}';
    }

    public static final boolean[] noWeekDays()
    {
        return new boolean[] { false, false, false, false, false, false, false };
    }

    public static final boolean[] allWeekDays()
    {
        return new boolean[] { true, true, true, true, true, true, true };
    }

    public String displayDose() {
        int integerPart = (int) dose;
        double fraction = dose - integerPart;

        String fractionRational;
        if (fraction == 0.125) {
            fractionRational = "1/8";
        } else if (fraction == 0.25) {
            fractionRational = "1/4";
        } else if (fraction == 0.5) {
            fractionRational = "1/2";
        } else if (fraction == 0.75) {
            fractionRational = "3/4";
        } else if (fraction == 0) {
            return "" + ((int) dose);
        } else {
            return "" + dose;
        }
        return integerPart + "+" + fractionRational;
    }

    public DateTime startDateTime() {
        LocalDate s = start != null ? start : LocalDate.now();
        LocalTime t = startTime != null ? startTime : LocalTime.MIDNIGHT;
        return s.toDateTime(t);
    }

    public void setCycle(int days, int rest) {
        this.cycle = days + "," + rest;
    }

    public int getCycleDays() {
        if (cycle == null) {
            return 0;
        }
        String[] parts = cycle.split(",");
        return Integer.valueOf(parts[0]);
    }

    public int getCycleRest() {
        if (cycle == null) {
            return 0;
        }
        String[] parts = cycle.split(",");
        return Integer.valueOf(parts[1]);
    }

    public boolean scanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }
}

