package es.usc.citius.servando.calendula.persistence;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by joseangel.pineiro on 10/9/14.
 */
@Table(name = "DailySchedules")
public class DailySchedule extends Model {

    public static final String COLUMN_DATE = "Day";

    @Column(name = COLUMN_DATE)
    private DateTime date;

    public DailySchedule() {

    }

    public DailySchedule(DateTime date) {
        this.date = date;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }


    public List<DailyScheduleItem> items() {
        return getMany(DailyScheduleItem.class, DailyScheduleItem.COLUMN_DAILY_SCHEDULE);
    }

    ;


    public static final DailySchedule fromDate(DateTime date) {
        return new Select().from(DailySchedule.class)
                .where(COLUMN_DATE + "= ?", date.toString("dd/mm/yyyy"))
                .executeSingle();

    }

    public static List<DailySchedule> findAll() {
        return new Select().from(DailySchedule.class)
                .orderBy(COLUMN_DATE + " DESC")
                .execute();
    }


}
