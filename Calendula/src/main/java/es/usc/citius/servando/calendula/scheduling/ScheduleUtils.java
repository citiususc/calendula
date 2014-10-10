package es.usc.citius.servando.calendula.scheduling;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

;

/**
 * Created by joseangel.pineiro on 7/8/14.
 */
public class ScheduleUtils {

    public static String[] dayNames = new String[]{"Mon", "Tue", "Wed", "Thu", "Fry", "Sat", "Sun"}; // 7 days

    public static String getTimesStr(int items) {

        switch (items) {
            case 0:
                return "Never";
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
                return items + " times a day";
        }

    }

    /**
     * Obtains the doses (Schedule Items) that are attached to a routine
     *
     * @param routine The routine
     * @return
     */
    public static List<ScheduleItem> getRoutineScheduleItems(Routine routine, boolean includeTaken) {

        // get day of week to filter results
        int TODAY_IN_WEEK = DateTime.now().getDayOfWeek(); // ISO8601: MON = 1, TUE = 2...
        // iterate over routine items and filter by day
        List<ScheduleItem> doses = new ArrayList<ScheduleItem>();
        for (ScheduleItem scheduleItem : routine.scheduleItems()) {
            DailyScheduleItem dsi = DailyScheduleItem.findByScheduleItem(scheduleItem);
            boolean takenToday = dsi.takenToday();
            if (scheduleItem.schedule().enabledFor(TODAY_IN_WEEK)) {
                if (includeTaken || (!includeTaken && !takenToday)) {
                    doses.add(scheduleItem);
                }
            }
        }
        return doses;
    }

    /**
     * Obtains the doses (Schedule Items) for an hour of the day
     *
     * @param hour The hour to get items from
     * @return
     */
    public static List<ScheduleItem> getHourScheduleItems(int hour, boolean includeTaken) {

        List<ScheduleItem> doses = new ArrayList<ScheduleItem>();
        // iterate over schedules
        for (Routine r : Routine.findAll()) {
            if (r.time().getHourOfDay() == hour) {
                doses.addAll(getRoutineScheduleItems(r, includeTaken));
            }
        }
        return doses;
    }

    public static String stringifyDays(String[] days) {
        String dayStr = "";
        for (int i = 0; i < days.length - 1; i++) {
            if (i > 0)
                dayStr += ", ";
            dayStr += days[i];
        }
        return dayStr + ((days.length > 1 ? " and " : "") + days[days.length - 1]);
    }

    public static String stringifyDays(boolean[] checkedDays) {

        String[] days = getSelectedDays(checkedDays);

        if (days.length == 7) {
            return "Every day";//TODO get from resources
        } else if (days.length == 0) {
            return "Never";//TODO get from resources
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

}
