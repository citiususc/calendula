package es.usc.citius.servando.calendula.persistence;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;


/**
 * Created by joseangel.pineiro on 12/17/13.
 */
@Table(name = "Schedules")
public class Schedule extends Model {

    public static final String COLUMN_MEDICINE = "Medicine";
    public static final String COLUMN_DAYS = "Days";

    @Column(name = COLUMN_MEDICINE)
    private Medicine medicine;

    @Column(name = COLUMN_DAYS)
    private boolean[] days = new boolean[]{true, true, true, true, true, true, true};

    public Schedule() {

    }

    public Schedule(Medicine medicine) {
        this.medicine = medicine;
    }

    public Schedule(Medicine medicine, boolean[] days) {
        this.medicine = medicine;
        this.days = days;
    }

    /**
     * Get the schedule items
     *
     * @return the items associated to this schedule
     */
    public List<ScheduleItem> items() {
        return getMany(ScheduleItem.class, ScheduleItem.COLUMN_SCHEDULE);
    }

    public Medicine medicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public boolean[] days() {
        return days;
    }

    public void setDays(boolean[] days) {
        this.days = days;
    }

    public boolean enabledFor(int dayOfWeek) {
        if (dayOfWeek > 7 || dayOfWeek < 1)
            throw new IllegalArgumentException("Day off week must be between 1 and 7");

        return days[dayOfWeek - 1];
    }
    //
    // DB queries
    //

    public static List<Schedule> findAll() {
        return new Select().from(Schedule.class)
                .orderBy(COLUMN_MEDICINE + " ASC")
                .execute();
    }
}

