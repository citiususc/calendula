package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalTimePersister;

/**
 * Created by joseangel.pineiro
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

    public Routine() {
    }

    public Routine(LocalTime time, String name) {
        this.time = time;
        this.name = name;
    }

    public static List<Routine> findAll() {
        return DB.routines().findAll();
    }

    public static Routine findById(long id) {
        return DB.routines().findById(id);
    }

    public static Routine findByName(String name) {
        return DB.routines().findOneBy(COLUMN_NAME, name);
    }

    public static List<Routine> findInHour(int hour) {
        return DB.routines().findInHour(hour);
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

    // *************************************
    // DB queries
    // *************************************

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void save() {
        DB.routines().save(this);
    }

    public void deleteCascade() {
        DB.routines().deleteCascade(this, false);
    }

    public List<ScheduleItem> scheduleItems() {
        return DB.scheduleItems().findByRoutine(this);
    }


}
