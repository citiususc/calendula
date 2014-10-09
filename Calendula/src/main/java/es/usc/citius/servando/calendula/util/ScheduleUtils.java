package es.usc.citius.servando.calendula.util;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.DailyDosageChecker;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.store.ScheduleStore;

/**
 * Created by joseangel.pineiro on 7/8/14.
 */
public class ScheduleUtils {

    public static String getTimesStr(List<ScheduleItem> items) {

        if(items == null || items.size() == 0){
            throw new IllegalArgumentException("Routines list can not be null or empty");
        }
        //TODO: move to values.xml
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


    public static boolean hasSchedules(Routine routine) {

        // iterate over schedules
        for (Schedule schedule : ScheduleStore.instance().getSchedules()) {
            // iterate over schedule items and check for current routine
            for (ScheduleItem item : schedule.items()) {
                if (item.routineId().equals(routine.id())) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Obtains the doses (Schedule Items) that are attached to a routine
     * @param routine The routine
     * @return
     */
    public static Map<Schedule,ScheduleItem> getRoutineScheduleItems(Routine routine, boolean includeTaken){

        Map<Schedule,ScheduleItem> doses = new HashMap<Schedule, ScheduleItem>();

        int TODAY_IN_WEEK = DateTime.now().getDayOfWeek(); // ISO8601: MON = 1, TUE = 2...

        // iterate over schedules
        for (Schedule schedule : ScheduleStore.instance().getSchedules()) {
            // check if schedule is enabled for today
            if(schedule.enabledForDay(TODAY_IN_WEEK)){
                // iterate over schedule items and check for current routine
                for(ScheduleItem item : schedule.items()){
                    if(item.routineId().equals(routine.id())){
                        if (includeTaken || (!includeTaken && !DailyDosageChecker.instance().doseTaken(item))) {
                            // we need to add this item to the list of
                            // schedule items to execute now
                            doses.put(schedule, item);
                            break;
                        }
                    }
                }
            }
        }
        return doses;
    }


    /**
     * Obtains the doses (Schedule Items) for an hour of the dayto a routine
     *
     * @param hour The hour to get items from
     * @return
     */
    public static Map<Schedule, ScheduleItem> getHourScheduleItems(int hour, boolean includeTaken) {
        Map<Schedule, ScheduleItem> doses = new HashMap<Schedule, ScheduleItem>();
        int TODAY_IN_WEEK = DateTime.now().getDayOfWeek(); // ISO8601: MON = 1, TUE = 2...
        // iterate over schedules
        for (Schedule schedule : ScheduleStore.instance().getSchedules()) {
            // check if schedule is enabled for today
            if (schedule.enabledForDay(TODAY_IN_WEEK)) {
                // iterate over schedule items and check for current routine
                for (ScheduleItem item : schedule.items()) {
                    Routine r = RoutineStore.instance().get(item.routineId());
                    if (r.getTime().getHourOfDay() == hour) {
                        if(includeTaken || (!includeTaken && !DailyDosageChecker.instance().doseTaken(item))) {
                            // we need to add this item to the list of
                            // schedule items to execute now
                            doses.put(schedule, item);
                            break;
                        }
                    }
                }
            }
        }
        return doses;
    }

    public static String getDaysStr(String[] days) {
        String dayStr = "";
        for (int i = 0; i < days.length - 1; i++) {
            if (i > 0)
                dayStr += ", ";
            dayStr += days[i];
        }
        return dayStr + ((days.length > 1 ? " and " : "") + days[days.length - 1]);
    }

    public static String getDaysStr(boolean[] checkedDays) {

        String[] days = getSelectedDays(checkedDays);

        if (days.length == 7) {
            return "Every day";//TODO get from resources
        }

        String dayStr = "";
        for (int i = 0; i < days.length - 1; i++) {
            if (i > 0)
                dayStr += ", ";
            dayStr += days[i];
        }
        return dayStr + ((days.length > 1 ? " and " : "") + days[days.length - 1]);
    }

    public static String[] getSelectedDays(boolean[] days) {
        ArrayList<String> sdays = new ArrayList<String>();

        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                sdays.add(ScheduleUtils.dayNames[i]);
            }
        }
        return sdays.toArray(new String[sdays.size()]);

    }


    public static String[] dayNames = new String[]{"Mon", "Tue", "Wed", "Thu", "Fry", "Sat", "Sun"}; // 7 days

}
