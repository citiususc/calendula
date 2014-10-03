package es.usc.citius.servando.calendula.util;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.store.ScheduleStore;

/**
 * Created by joseangel.pineiro on 7/8/14.
 */
public class ScheduleUtils {

    public static String getTimesStr(List<ScheduleItem> items) {

        if(items == null || items.size() == 0){
            throw new IllegalArgumentException("Routines list can not be null or empty");
        }

        switch (items.size()) {
            case 1:
                return "Once a day";
            case 2:
                return "Twice a day";
            case 3:
                return "Three times a day";
            case 4:
                return "Four times a day";
            case 5:
                return "Five times a day";
            default:
                return items.size() + " times a day";
        }

    }


    /**
     * Obtains the doses (Schedule Items) that are attached to a routine
     * @param routine The routine
     * @return
     */
    public static Map<Schedule,ScheduleItem> getRoutineScheduleItems(Routine routine){

        Map<Schedule,ScheduleItem> doses = new HashMap<Schedule, ScheduleItem>();

        int TODAY_IN_WEEK = DateTime.now().getDayOfWeek(); // ISO8601: MON = 1, TUE = 2...

        // iterate over schedules
        for (Schedule schedule : ScheduleStore.instance().getSchedules()) {
            // check if schedule is enabled for today
            if(schedule.enabledForDay(TODAY_IN_WEEK)){
                // iterate over schedule items and check for current routine
                for(ScheduleItem item : schedule.items()){
                    if(item.routineId().equals(routine.id())){
                        // we need to add this item to the list of
                        // schedule items to execute now
                        doses.put(schedule,item);
                        break;
                    }
                }
            }
        }
        return doses;
    }
}
