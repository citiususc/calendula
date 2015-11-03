package es.usc.citius.servando.calendula.util;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class DailyAgendaItemStub {

    public static final String TAG = DailyAgendaItemStub.class.getName();
    public boolean isSpacer = false;
    public boolean isEmptyPlaceholder = false;
    public boolean isRoutine = true;
    public boolean isNext = false;
    public boolean hasEvents;
    public List<DailyAgendaItemStubElement> meds;
    public String title = "";
    public LocalTime time;
    public Long id = -1l;
    public int hour;
    public int minute;
    public Patient patient;

    public DailyAgendaItemStub(LocalTime time)
    {
        this.time = time;
    }

    public static List<DailyAgendaItemStub> fromHour(int hour)
    {
        
        //int today = DateTime.now().getDayOfWeek();
        LocalDate today = LocalDate.now();

        List<Routine> routines = Routine.findInHour(hour);
        List<DailyAgendaItemStub> items = new ArrayList<DailyAgendaItemStub>(routines.size());

        if (!routines.isEmpty())
        {

            for (Routine r : routines) {
                // Find doses off all routines in this hour
                List<ScheduleItem> doses = r.scheduleItems();

                if (doses.size() > 0)
                {

                    // create an ItemStub for the current hour
                    DailyAgendaItemStub item = new DailyAgendaItemStub(r.time());

                    item.meds = new ArrayList<>();

                    for (ScheduleItem scheduleItem : doses) {
                        if (scheduleItem.schedule() != null && scheduleItem.schedule()
                                .enabledForDate(today)) {
                            item.hasEvents = true;
                            int minute = r.time().getMinuteOfHour();
                            Medicine med = scheduleItem.schedule().medicine();
                            DailyAgendaItemStubElement el = new DailyAgendaItemStubElement();
                            el.medName = med.name();
                            el.dose = scheduleItem.dose();
                            el.displayDose = scheduleItem.displayDose();
                            el.res = med.presentation().getDrawable();
                            el.presentation = med.presentation();
                            el.minute = minute < 10 ? "0" + minute : String.valueOf(minute);
                            el.taken = DailyScheduleItem.findByScheduleItem(scheduleItem).takenToday();
                            item.meds.add(el);
                        }
                    }
                    Collections.sort(item.meds);

                    if (!item.meds.isEmpty())
                    {
                        item.id = r.getId();
                        item.patient = r.patient();
                        item.title = r.name();
                        item.hour = r.time().getHourOfDay();
                        item.minute = r.time().getMinuteOfHour();
                        items.add(item);
                    }
                } else
                {
                    items.add(new DailyAgendaItemStub(new LocalTime(hour, 0)));
                }
            }
        } else
        {
            items.add(new DailyAgendaItemStub(new LocalTime(hour, 0)));
        }

        return items;
    }

    public static class DailyAgendaItemStubElement
            implements Comparable<DailyAgendaItemStubElement> {

        public String medName;
        public String minute;
        public String displayDose;
        public double dose;

        public boolean taken;
        public Presentation presentation;
        public int res;

        @Override
        public int compareTo(DailyAgendaItemStubElement other)
        {
            int result = minute.compareTo(other.minute);
            if (result == 0) result = taken ? 0 : 1;
            if (result == 0) result = medName.compareTo(other.medName);

            return result;
        }
    }
}
