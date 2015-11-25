package es.usc.citius.servando.calendula.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Presentation;

/**
 * Stub for daily agenda view
 */
public class DailyAgendaItemStub {

    public static final String TAG = "DailyAgendaItemStub";

    public List<DailyAgendaItemStubElement> meds;

    public String title = "";
    public LocalTime time;
    public LocalDate date;
    public Long id = -1l;
    public Patient patient;

    public boolean isSpacer = false;
    public boolean isRoutine = true;
    public boolean hasEvents = false;
    public boolean displayable = false;
    public boolean isCurrentHour = false;

    public DailyAgendaItemStub(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    public DateTime dateTime() {
        return date.toDateTime(time);
    }

    public static class DailyAgendaItemStubElement  implements Comparable<DailyAgendaItemStubElement> {

        public int res;
        public double dose;
        public boolean taken;

        public String medName;
        public String minute;
        public String displayDose;
        public Long scheduleItemId = -1l;
        public Presentation presentation;

        @Override
        public int compareTo(DailyAgendaItemStubElement other) {
            int result = minute.compareTo(other.minute);
            if (result == 0) result = taken ? 0 : 1;
            if (result == 0) result = medName.compareTo(other.medName);
            return result;
        }
    }

    @Override
    public String toString() {
        return "DailyAgendaItemStub{" +
                ", isRoutine=" + isRoutine +
                ", hasEvents=" + hasEvents +
                ", count=" + (meds != null ? meds.size() : 0) +
                ", title='" + title + '\'' +
                ", time=" + time.toString("kk:mm") +
                ", date=" + date.toString("dd/MM") +
                '}';
    }

}
