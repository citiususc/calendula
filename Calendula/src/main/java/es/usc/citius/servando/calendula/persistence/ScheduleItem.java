package es.usc.citius.servando.calendula.persistence;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
@Table(name = "ScheduleItems")
public class ScheduleItem extends Model {

    public static final String COLUMN_SCHEDULE = "Schedule";
    public static final String COLUMN_ROUTINE = "Routine";
    public static final String COLUMN_DOSE = "Dose";

    @Column(name = COLUMN_SCHEDULE)
    private Schedule schedule;

    @Column(name = COLUMN_ROUTINE)
    private Routine routine;

    @Column(name = COLUMN_DOSE)
    private float dose;

    public ScheduleItem() {

    }

    public ScheduleItem(Schedule schedule, Routine routine, float dose) {
        this.schedule = schedule;
        this.routine = routine;
        this.dose = dose;
    }

    public ScheduleItem(Schedule schedule, Routine routine) {
        this.schedule = schedule;
        this.routine = routine;
    }

    public Routine getRoutine() {
        return routine;
    }

    public void routine(Routine routine) {
        this.routine = routine;
    }

    public Schedule schedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public float dose() {
        return dose;
    }

    public void setDose(float dose) {
        this.dose = dose;
    }


}
