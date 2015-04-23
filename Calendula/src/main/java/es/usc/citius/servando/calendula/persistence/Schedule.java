package es.usc.citius.servando.calendula.persistence;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrence;
import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrenceFormatter;
import com.google.ical.values.DateValue;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.typeSerializers.BooleanArrayPersister;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalDatePersister;
import es.usc.citius.servando.calendula.persistence.typeSerializers.RRulePersister;


/**
 * Created by joseangel.pineiro
 */
@DatabaseTable(tableName = "Schedules")
public class Schedule {

    public static final int SCHEDULE_TYPE_EVERYDAY = 0; // DEFAULT
    public static final int SCHEDULE_TYPE_SOMEDAYS = 1;
    public static final int SCHEDULE_TYPE_INTERVAL = 2;
    public static final int SCHEDULE_TYPE_CUSTOM = 3;
    public static final int SCHEDULE_TYPE_HOURLY = 4;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MEDICINE = "Medicine";
    public static final String COLUMN_DAYS = "Days";
    public static final String COLUMN_RRULE = "Rrule";
    public static final String COLUMN_START = "Start";
    public static final String COLUMN_DOSE = "Dose";
    public static final String COLUMN_TYPE = "Type";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_MEDICINE, foreign = true, foreignAutoRefresh = true)
    private Medicine medicine;

    @DatabaseField(columnName = COLUMN_DAYS, persisterClass = BooleanArrayPersister.class)
    private boolean[] days = new boolean[]{false, false, false, false, false, false, false};

    @DatabaseField(columnName = COLUMN_RRULE, persisterClass = RRulePersister.class)
    private RepetitionRule rrule;

    @DatabaseField(columnName = COLUMN_START, persisterClass = LocalDatePersister.class)
    private LocalDate start;

    @DatabaseField(columnName = COLUMN_DOSE)
    private float dose = 1f;

    @DatabaseField(columnName = COLUMN_TYPE)
    private int type = SCHEDULE_TYPE_EVERYDAY;


    public RepetitionRule rule() {
        return rrule;
    }

    public void setRepetition(RepetitionRule rrule) {
        this.rrule = rrule;
    }

    public int type() {
        return type;
    }

    public void setType(int type) {
        if (type < 0 || type > 4) {
            throw new RuntimeException("Invalid schedule type");
        }
        this.type = type;
    }

    public Schedule() {
        rrule = new RepetitionRule(null);
    }

    public Schedule(Medicine medicine) {
        this.medicine = medicine;
    }

    public Schedule(Medicine medicine, boolean[] days) {
        this.medicine = medicine;
        setDays(days);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ScheduleItem> items() {
        return DB.scheduleItems().findBySchedule(this);
    }

    public List<DateTime> hourlyItemsToday() {
        DateTime today = DateTime.now().withTimeAtStartOfDay();
        // get schedule occurrences for the current day
        return rrule.occurrencesBetween(today, today.plusDays(1));
    }

    public Medicine medicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public boolean[] days() {
        return rrule.days();
    }

    public void setDays(boolean[] days) {
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

    public void setDose(float dose) {
        this.dose = dose;
    }


    public boolean enabledForDate(LocalDate date) {
        boolean enabled = rrule.hasOccurrencesAt(date);
        Log.d("Schedule", "------ Schedule " + medicine().name() + " enabled for " + date.toString("dd/MM/YY") + ": " + enabled);
        return enabled;
    }

    public List<LocalDate> ocurrencesBetween(LocalDate start, LocalDate end) {
        return rrule.occurrencesBetween(start, end);
    }

    public String toReadableString(Context ctx) {

        EventRecurrence e = new EventRecurrence();
        Time t;
        if (start != null) {
            t = new Time();
            t.set(start.getDayOfWeek(), start.getMonthOfYear(), start.getYear());
        } else {
            t = new Time();
            t.setToNow();
            t.normalize(true);
            e.setStartDate(t);
        }
        String ical = rrule.toIcal();
        if (ical != null)
            e.parse(ical.replace("RRULE:", ""));

        return EventRecurrenceFormatter.getRepeatString(ctx, ctx.getResources(), e, false);
    }

    // *************************************
    // DB queries
    // *************************************

    public static List<Schedule> findAll() {
        return DB.schedules().findAll();
    }

    public static List<Schedule> findByMedicine(Medicine med) {
        return DB.schedules().findByMedicine(med);
    }

    public static Schedule findById(long id) {
        return DB.schedules().findById(id);
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
            if (!d)
                return false;
        return true;
    }

    public boolean repeatsHourly() {
        return type == SCHEDULE_TYPE_HOURLY;
    }

    public int dayCount() {
        int count = 0;
        for (boolean d : days())
            if (d)
                count += 1;
        return count;
    }

    public void toggleSelectedDay(int i) {
        boolean[] d = days();
        d[i] = !d[i];
        rrule.setDays(d);
        Log.d("Schedule", "Days: " + Arrays.toString(days()));
    }

    public LocalDate end() {
        DateValue v = rrule.iCalRule().getUntil();
        return v != null ? new LocalDate(v.year(), v.month(), v.day()) : null;
    }

    public DateValue until() {
        return rrule.iCalRule().getUntil();
    }

}

