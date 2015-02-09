package es.usc.citius.servando.calendula.persistence;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.List;

/**
 * Created by castrelo on 4/10/14.
 */
@Table(name = "DailyScheduleItems", id = DailyScheduleItem.COLUMN_ID)
public class DailyScheduleItem extends Model {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCHEDULE_ITEM = "ScheduleItem";

    public static final String COLUMN_DATE = "Date";
    public static final String COLUMN_TAKEN_TODAY = "TakenToday";
    public static final String COLUMN_TIME_TAKEN = "TimeTaken";

//    @Column(name = COLUMN_DATE)
//    private DateTime date;

    @Column(name = COLUMN_SCHEDULE_ITEM, onDelete = Column.ForeignKeyAction.NO_ACTION, onUpdate = Column.ForeignKeyAction.NO_ACTION)
    private ScheduleItem scheduleItem;

    @Column(name = COLUMN_TAKEN_TODAY)
    private boolean takenToday;

    @Column(name = COLUMN_TIME_TAKEN)
    private LocalTime timeTaken;

    public DailyScheduleItem() {
    }

    public DailyScheduleItem(DateTime date, ScheduleItem scheduleItem) {
//        this.date = date;
        this.scheduleItem = scheduleItem;
    }

    public DailyScheduleItem(ScheduleItem scheduleItem) {
//        this.date = scheduleItem.routine().time().toDateTimeToday();
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

    public void setTimeTaken(LocalTime date) {
        this.timeTaken = date;
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


    public static DailyScheduleItem findById(long id) {
        return new Select().from(DailyScheduleItem.class)
                .where(COLUMN_ID + " = ?", id)
                .executeSingle();
    }


    public static List<DailyScheduleItem> findAll() {
        return new Select().from(DailyScheduleItem.class)
                .execute();
    }

    public static DailyScheduleItem findByScheduleItem(ScheduleItem item) {
        return new Select().from(DailyScheduleItem.class)
                .where(COLUMN_SCHEDULE_ITEM + " = ?", item.getId())
                .executeSingle();
    }

    public static List<DailyScheduleItem> fromDate(DateTime date) {
        // get one day interval
        String start = date.withTimeAtStartOfDay().toString("yy/mm/dd kk:mm");
        String end = date.plusDays(1).withTimeAtStartOfDay().toString("yy/mm/dd kk:mm");

        return new Select().from(DailyScheduleItem.class)
                .where(COLUMN_DATE + " BETWEEN ? AND ?", start, end)
                .execute();
    }

    public static void removeAll() {
        for (DailyScheduleItem i : findAll()) {
            i.delete();
        }
    }
}

