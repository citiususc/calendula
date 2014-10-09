package es.usc.citius.servando.calendula.persistence;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.joda.time.LocalTime;

/**
 * Created by castrelo on 4/10/14.
 */
@Table(name = "DailyScheduleItems")
public class DailyScheduleItem extends Model {

    public static final String COLUMN_DAILY_SCHEDULE_ITEM = "DailyScheduleItem";
    public static final String COLUMN_DAILY_SCHEDULE = "DailySchedule";
    public static final String COLUMN_TAKEN_TODAY = "TakenToday";
    public static final String COLUMN_TIME_TAKEN = "TimeTaken";

    @Column(name = COLUMN_DAILY_SCHEDULE)
    private DailySchedule dailySchedule;

    @Column(name = COLUMN_DAILY_SCHEDULE_ITEM)
    private ScheduleItem scheduleItem;

    @Column(name = COLUMN_TAKEN_TODAY)
    private boolean takenToday;

    @Column(name = COLUMN_TIME_TAKEN)
    private LocalTime timeTaken;

    public DailyScheduleItem() {

    }

    public DailyScheduleItem(DailySchedule dailySchedule, ScheduleItem scheduleItem) {
        this.dailySchedule = dailySchedule;
        this.scheduleItem = scheduleItem;
    }

    public DailyScheduleItem(ScheduleItem scheduleItem) {
        this.scheduleItem = scheduleItem;
    }

    public LocalTime timeTaken() {
        return timeTaken;
    }

    public ScheduleItem scheduleItem() {
        return scheduleItem;
    }

    public void setScheduleItem(ScheduleItem scheduleItem) {
        this.scheduleItem = scheduleItem;
    }


    public DailySchedule dailySchedule() {
        return dailySchedule;
    }

    public void setDailySchedule(DailySchedule dailySchedule) {
        this.dailySchedule = dailySchedule;
    }


    public boolean takenToday() {
        return takenToday;
    }

    public void setTakenToday(boolean takenToday) {
        this.takenToday = takenToday;
        if (takenToday) {
            timeTaken = LocalTime.now();
        }
    }

    @Override
    public String toString() {
        return "DailyScheduleItem{" +
                " med=" + scheduleItem.schedule().medicine().name() +
                " dose=" + scheduleItem.dose() +
                ", takenToday=" + takenToday +
                ", timeTaken=" + timeTaken +
                '}';
    }
}

