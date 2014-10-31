package es.usc.citius.servando.calendula.persistence;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.LocalTime;

import java.util.List;

/**
 * Created by joseangel.pineiro on 10/9/14.
 */
@Table(name = "Routines", id = Routine.COLUMN_ID)
public class Routine extends Model {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME = "Time";
    public static final String COLUMN_NAME = "Name";

    @Column(name = COLUMN_TIME)
    private LocalTime time;

    @Column(name = COLUMN_NAME)
    private String name;

    public Routine() {

    }

    public Routine(LocalTime time, String name) {
        this.time = time;
        this.name = name;
    }

    public LocalTime time() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //
    // DB queries
    //

    /**
     * Get the schedule items
     *
     * @return the items associated to this schedule
     */
    public List<ScheduleItem> scheduleItems() {
        return getMany(ScheduleItem.class, ScheduleItem.COLUMN_ROUTINE);
    }

    public static List<Routine> findAll() {
        return new Select().from(Routine.class)
                .orderBy(COLUMN_TIME + " ASC")
                .execute();
    }

    public static Routine findById(long id) {
        return new Select().from(Routine.class)
                .where(COLUMN_ID + " = ?", id)
                .executeSingle();
    }

    public static Routine findByName(String name) {
        return new Select().from(Routine.class)
                .where(COLUMN_NAME + " = ?", name)
                .executeSingle();
    }

    public static List<Routine> findInHour(int hour) {

        LocalTime time = new LocalTime(hour, 0);

        // get one hour interval [h:00, h:59:]
        String start = time.toString("kk:mm");
        String end = time.plusMinutes(59).toString("kk:mm");

        return new Select().from(Routine.class)
                .where(COLUMN_TIME + " BETWEEN ? AND ?", start, end)
                .execute();
    }

    public void deleteCascade() {

        // Temporal fix for https://github.com/pardom/ActiveAndroid/issues/188.
        // Set schedule and routine foreign keys to null

        List<ScheduleItem> items = scheduleItems();
        for (ScheduleItem i : items) {
//            i.setSchedule(null);
//            i.setRoutine(null);
//            i.save();
            i.deleteCascade();
        }
        this.delete();
    }

}
