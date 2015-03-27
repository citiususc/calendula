package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalTime;

import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalTimePersister;

/**
 * Created by castrelo on 4/10/14.
 */
@DatabaseTable(tableName = "DailyScheduleItems")
public class DailyScheduleItem {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCHEDULE_ITEM = "ScheduleItem";

    //public static final String COLUMN_DATE = "Date";
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return DB.DailyScheduleItems.findById(id);
    }


    public static List<DailyScheduleItem> findAll() {
        return DB.DailyScheduleItems.findAll();
    }

    public static DailyScheduleItem findByScheduleItem(ScheduleItem item) {
        return DB.DailyScheduleItems.findByScheduleItem(item);
    }

    public static void removeAll() {
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (DailyScheduleItem i : findAll()) {
                    DB.DailyScheduleItems.remove(i);
                }
                return null;
            }
        });        
    }

    public void save() {
        DB.DailyScheduleItems.save(this);
    }


}

