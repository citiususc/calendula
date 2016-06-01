package es.usc.citius.servando.calendula.adherence;

import android.util.Log;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * 
 */
public class AdherenceSummaryItem {

    public static final DateTimeFormatter DTF = ISODateTimeFormat.dateTimeNoMillis();

    public Long scheduleId;
    public String drug;
    public String dateTime;
    public String timeTaken;
    public Float dose;
    public boolean taken;
    public boolean canceledByUser;
    public Long patient;


    public AdherenceSummaryItem(DailyScheduleItem item) {

        Log.d("TEST", item.toString());

        Schedule s = item.boundToSchedule() ? item.schedule() : DB.schedules().findById(item.scheduleItem().schedule().getId());
        ScheduleItem si = item.scheduleItem(); // only for items not bound to schedule
        Medicine m = s.medicine();
        patient = item.patient().id();
        drug = m.cn();
        dose = item.boundToSchedule() ? s.dose() : si.dose();
        taken = item.takenToday();
        canceledByUser = item.timeTaken() != null && !item.takenToday();
        timeTaken = item.takenToday() ? item.date().toDateTime(item.timeTaken()).toString(DTF) : null;
        dateTime = item.date().toDateTime(item.time()).toString(DTF);
        scheduleId = s.getId();
        Log.d("TEST", toString());
    }

    @Override
    public String toString() {
        return "AdherenceSummaryItem{" +
                "scheduleId=" + scheduleId +
                ", drug='" + drug + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", timeTaken='" + timeTaken + '\'' +
                ", dose=" + dose +
                ", taken=" + taken +
                ", canceledByUser=" + canceledByUser +
                '}';
    }
}
