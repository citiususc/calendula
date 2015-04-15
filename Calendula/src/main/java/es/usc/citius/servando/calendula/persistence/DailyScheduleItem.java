package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalTimePersister;

/**
 * Created by castrelo
 */
@DatabaseTable(tableName = "DailyScheduleItems")
public class DailyScheduleItem {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCHEDULE_ITEM = "ScheduleItem";
    public static final String COLUMN_TAKEN_TODAY = "TakenToday";
    public static final String COLUMN_TIME_TAKEN = "TimeTaken";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_SCHEDULE_ITEM, foreign = true, foreignAutoRefresh = true)
    private ScheduleItem scheduleItem;

    @DatabaseField(columnName = COLUMN_TAKEN_TODAY)
    private boolean takenToday;

    @DatabaseField(columnName = COLUMN_TIME_TAKEN, persisterClass = LocalTimePersister.class)
    private LocalTime timeTaken;

    public DailyScheduleItem() {
    }

    public DailyScheduleItem(ScheduleItem scheduleItem) {
        this.scheduleItem = scheduleItem;
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
        } else {
            timeTaken = null;
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
        return DB.dailyScheduleItems().findById(id);
    }


    public static List<DailyScheduleItem> findAll() {
        return DB.dailyScheduleItems().findAll();
    }

    public static DailyScheduleItem findByScheduleItem(ScheduleItem item) {
        return DB.dailyScheduleItems().findByScheduleItem(item);
    }

    public void save() {
        DB.dailyScheduleItems().save(this);
    }


}

