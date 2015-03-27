package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalTime;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalTimePersister;

/**
 * Created by joseangel.pineiro on 10/9/14.
 */
@DatabaseTable(tableName = "Routines")
public class Routine {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME = "Time";
    public static final String COLUMN_NAME = "Name";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_TIME, persisterClass = LocalTimePersister.class)
    private LocalTime time;

    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

//    @ForeignCollectionField(foreignFieldName = "routine")
//    Collection<ScheduleItem> items;

    public Routine() {

    }

    public Routine(LocalTime time, String name) {
        this.time = time;
        this.name = name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    public void save() {
        DB.Routines.save(this);
    }

    public void deleteCascade() {

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Collection<ScheduleItem> items = scheduleItems();
                for (ScheduleItem i : items) {
                    i.deleteCascade();
                }
                DB.Routines.remove(Routine.this);
                return null;
            }
        });
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
        return DB.ScheduleItems.findByRoutine(this);
    }

    public static List<Routine> findAll() {
        return DB.Routines.findAll();
    }

    public static Routine findById(long id) {
        return DB.Routines.findById(id);
    }

    public static Routine findByName(String name) {
        return DB.Routines.findOneBy(COLUMN_NAME, name);
    }

    public static List<Routine> findInHour(int hour) {
        return DB.Routines.findInHour(hour);
    }


}
