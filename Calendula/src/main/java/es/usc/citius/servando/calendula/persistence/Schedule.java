package es.usc.citius.servando.calendula.persistence;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalDate;

import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.typeSerializers.BooleanArrayPersister;
import es.usc.citius.servando.calendula.persistence.typeSerializers.RRulePersister;


/**
 * Created by joseangel.pineiro
 */
@DatabaseTable(tableName = "Schedules")
public class Schedule {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MEDICINE = "Medicine";
    public static final String COLUMN_DAYS = "Days";
    public static final String COLUMN_RRULE = "Rrule";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_MEDICINE, foreign = true, foreignAutoRefresh = true)
    private Medicine medicine;

    @DatabaseField(columnName = COLUMN_DAYS, persisterClass = BooleanArrayPersister.class)
    private boolean[] days = new boolean[]{true, true, true, true, true, true, true};

    public RepetitionRule getRepetition() {
        return rrule;
    }

    public void setRepetition(RepetitionRule rrule) {
        this.rrule = rrule;
    }

    /**
     * See: http://google-rfc-2445.googlecode.com/svn/trunk/rfc2445.html
     */
    @DatabaseField(columnName = COLUMN_RRULE, persisterClass = RRulePersister.class)
    private RepetitionRule rrule;

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

    /**
     * @deprecated Use enabledForDate instead
     */
    private boolean enabledFor(int dayOfWeek) {
        if (dayOfWeek > 7 || dayOfWeek < 1)
            throw new IllegalArgumentException("Day off week must be between 1 and 7");

        return days()[dayOfWeek - 1];
    }

    public boolean enabledForDate(LocalDate date) {

        boolean enabled = rrule.hasOccurrencesAt(date);
        Log.d("Schedule", "------ Schedule " + medicine().name() + " enabled for " + date.toString("dd/MM/YY") + ": " + enabled);
        return enabled;
    }

    public List<LocalDate> ocurrencesBetween(LocalDate start, LocalDate end) {
        return rrule.occurrencesBetween(start, end);
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


}

