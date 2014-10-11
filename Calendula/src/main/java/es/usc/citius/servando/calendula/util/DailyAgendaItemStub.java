package es.usc.citius.servando.calendula.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;


/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class DailyAgendaItemStub {

    public static final String TAG = DailyAgendaItemStub.class.getName();

    public boolean isSpacer=false;

    public int hour;
    public boolean hasEvents;
    public List<DailyAgendaItemStubElement> meds;

    public int primaryColor = -1;
    public int secondaryColor = -1;
    public boolean hasColors = false;

    public DailyAgendaItemStub(int hour) {
        this.hour = hour;
    }


    public static DailyAgendaItemStub fromRoutine(int hour) {
        // create an ItemStub for the current hour
        DailyAgendaItemStub item = new DailyAgendaItemStub(hour);
        // find routines in this our
        List<Routine> routines = Routine.findInHour(hour);
        // Find doses off all routines in this hour
        List<ScheduleItem> doses = new ArrayList<ScheduleItem>();
        for (Routine routine : routines) {
            doses.addAll(routine.scheduleItems());
        }
        if (doses.size() > 0) {
            item.hasEvents = true;
            item.meds = new ArrayList<DailyAgendaItemStubElement>();
            for (ScheduleItem scheduleItem : doses) {

                int minute = scheduleItem.routine().time().getMinuteOfHour();

                Medicine med = scheduleItem.schedule().medicine();
                DailyAgendaItemStubElement el = new DailyAgendaItemStubElement();
                el.medName = med.name();
                el.dose = String.valueOf((int) scheduleItem.dose());
                el.minute = minute < 10 ? "0" + minute : String.valueOf(minute);
                el.taken = DailyScheduleItem.findByScheduleItem(scheduleItem).takenToday();
                item.meds.add(el);
            }
            Collections.sort(item.meds);
        }
        Log.d(TAG, "Schedules in hour: " + doses.size());
        return item;
    }

    public static class DailyAgendaItemStubElement implements Comparable<DailyAgendaItemStubElement> {

        public String medName;
        public String minute;
        public String dose;
        public boolean taken;

        @Override
        public int compareTo(DailyAgendaItemStubElement other) {
            int result = minute.compareTo(other.minute);
            if (result == 0)
                result = medName.compareTo(other.medName);
            return result;
        }
    }

}
