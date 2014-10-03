package es.usc.citius.servando.calendula.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import es.usc.citius.servando.calendula.model.Dose;
import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.store.RoutineStore;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class DailyAgendaItemStub {

    public static final String TAG = DailyAgendaItemStub.class.getName();
    public static String[] medNames = {"Ibuprofeno", "Ramipril", "Atrovent","Digoxina"};

    public int hour;
    public boolean hasEvents;
    public List<DailyAgendaItemStubElement> meds;

    public int primaryColor = -1;
    public int secondaryColor = -1;
    public boolean hasColors = false;

    public DailyAgendaItemStub(int hour){
        this.hour = hour;
    }


    public static DailyAgendaItemStub fromRoutine(int hour){

        Log.d(TAG, "Creating scheduleitem stub for hour: " + hour);

        DailyAgendaItemStub item = new DailyAgendaItemStub(hour);
        // find routines in this our
        List<Routine> routines = RoutineStore.instance().getInHour(hour);
        // for each routine, get doses
        Map<Schedule, List<ScheduleItem>> schedulesInHour = new HashMap<Schedule, List<ScheduleItem>>();

        Log.d(TAG, "Routines in hour: " + routines.size());

        for(Routine routine : routines){
            Map<Schedule, ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine);

            Log.d(TAG, "Schedule items for routine " + routine.getName() + ": " + doses.size());

            for (Schedule s : doses.keySet()) {
                if(!schedulesInHour.containsKey(s))
                    schedulesInHour.put(s, new ArrayList<ScheduleItem>());

                schedulesInHour.get(s).add(doses.get(s));
            }
        }

        Log.d(TAG, "Schedules in hour: " + schedulesInHour.size());

        if(schedulesInHour!=null && schedulesInHour.size() > 0) {
            item.hasEvents = true;
            item.meds = new ArrayList<DailyAgendaItemStubElement>();
            for (Schedule s : schedulesInHour.keySet()) {

                for (ScheduleItem scheduleItem : schedulesInHour.get(s)) {

                    Medicine med = s.getMedicine();
                    Routine rtn = RoutineStore.instance().get(scheduleItem.routineId());
                    int minute = rtn.getTime().getMinuteOfHour();

                    DailyAgendaItemStubElement el = new DailyAgendaItemStubElement();
                    el.medName = med.getName();
                    el.dose = String.valueOf(scheduleItem.dose().ammount());
                    el.minute = minute < 10 ? "0"+minute : String.valueOf(minute);
                    item.meds.add(el);
                }
            }
            Collections.sort(item.meds);
        }

        Log.d(TAG, "Schedules in hour: " + schedulesInHour.size());

        return item;
    }

    public static class DailyAgendaItemStubElement implements Comparable<DailyAgendaItemStubElement>{
        public String medName;
        public String minute;
        public String dose;

        @Override
        public int compareTo(DailyAgendaItemStubElement other) {
            int result = minute.compareTo(other.minute);
            if(result==0)
                result = medName.compareTo(other.medName);
            return result;
        }
        // ...
    }

}
