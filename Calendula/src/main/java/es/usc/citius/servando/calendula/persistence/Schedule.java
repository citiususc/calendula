package es.usc.citius.servando.calendula.persistence;

import android.content.Context;
import android.util.Log;

import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrence;
import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrenceFormatter;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalDate;

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

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MEDICINE = "Medicine";
    public static final String COLUMN_DAYS = "Days";
    public static final String COLUMN_RRULE = "Rrule";
    public static final String COLUMN_START = "Start";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_MEDICINE, foreign = true, foreignAutoRefresh = true)
    private Medicine medicine;

    @DatabaseField(columnName = COLUMN_DAYS, persisterClass = BooleanArrayPersister.class)
    private boolean[] days = new boolean[]{false, false, false, false, false, false, false};

    @DatabaseField(columnName = COLUMN_START, persisterClass = LocalDatePersister.class)
    private LocalDate start;

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

    public LocalDate start() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
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

    public String toReadableString(Context ctx) {

        EventRecurrence e = new EventRecurrence();
        String ical = rrule.toIcal();
        if (ical != null)
            e.parse(ical.replace("RRULE:", ""));

        return EventRecurrenceFormatter.getRepeatString(ctx, ctx.getResources(), e, true);

        /*int interval = rrule.iCalRule().getInterval();
        Frequency freq = rrule.iCalRule().getFreq();
        int byDaySize = rrule.iCalRule().getByDay() != null ? rrule.iCalRule().getByDay().size() : 0;

        if(freq.equals(Frequency.DAILY) && interval == 0 && byDaySize == 0){
            return ctx.getString(R.string.every_day);
        }else if (freq.equals(Frequency.DAILY) && (byDaySize > 0)){
            return ScheduleUtils.stringifyDays(days(),ctx);
        }else{
            String freqStr;
            String tail = "";
            if(freq == Frequency.DAILY){
                freqStr = ctx.getString(R.string.schedule_freq_days);
            }else if(freq == Frequency.WEEKLY){
                freqStr = ctx.getString(R.string.schedule_freq_weeks);
                tail = ScheduleUtils.stringifyDays(days(),ctx) + " " + ctx.getString(R.string.schedule_week_days_connector);
            }else{
                freqStr = ctx.getString(R.string.schedule_freq_months);
            }
            return ctx.getResources().getString(R.string.repeat_every_tostr, String.valueOf(interval), freqStr) + tail;
        }
*/

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

