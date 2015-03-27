package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
@DatabaseTable(tableName = "ScheduleItems")
public class ScheduleItem {
    
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCHEDULE = "Schedule";
    public static final String COLUMN_ROUTINE = "Routine";
    public static final String COLUMN_DOSE = "Dose";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_SCHEDULE, foreign = true, foreignAutoRefresh = true)
    private Schedule schedule;

    @DatabaseField(columnName = COLUMN_ROUTINE, foreign = true, foreignAutoRefresh = true)
    private Routine routine;

    @DatabaseField(columnName = COLUMN_DOSE)
    private float dose;

    public ScheduleItem() {
        super();
    }

    public ScheduleItem(Schedule schedule, Routine routine, float dose) {
        this();
        this.schedule = schedule;
        this.routine = routine;
        this.dose = dose;
    }

    public ScheduleItem(Schedule schedule, Routine routine) {
        this();
        this.schedule = schedule;
        this.routine = routine;
    }

    public void save() {
        DB.ScheduleItems.save(this);
    }

    public Routine routine() {
        return routine;
    }

    public void setRoutine(Routine routine) {
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

    public String displayDose() {
        int integerPart = (int) dose;
        double fraction = dose - integerPart;

        String fractionRational;
        if (fraction == 0.125)
            fractionRational = "1/8";
        else if (fraction == 0.25)
            fractionRational = "1/4";
        else if (fraction == 0.5)
            fractionRational = "1/2";
        else if (fraction == 0.75)
            fractionRational = "3/4";
        else if (fraction == 0)
            return "" + ((int) dose);
        else
            return "" + dose;
        return integerPart + "+" + fractionRational;

    }

    public void setDose(float dose) {
        this.dose = dose;
    }


    public void deleteCascade() {

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                DailyScheduleItem item = DailyScheduleItem.findByScheduleItem(ScheduleItem.this);
                DB.DailyScheduleItems.remove(item);
                DB.ScheduleItems.remove(ScheduleItem.this);
                return null;
            }
        });

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
