package es.usc.citius.servando.calendula.scheduling;

import android.content.Context;
import android.util.Log;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

;

/**
 * Created by joseangel.pineiro on 7/8/14.
 */
public class ScheduleUtils {


//    public static String[] dayNames = new String[]{"Mon", "Tue", "Wed", "Thu", "Fry", "Sat", "Sun"}; // 7 days
//
//    static {
//        DateFormatSymbols symbols = new DateFormatSymbols();
//        dayNames = symbols.getShortWeekdays();
//        for (int i = 0; i < dayNames.length; i++) {
//            dayNames[i] = dayNames[i].toUpperCase();
//        }
//    }

    public static String getTimesStr(int items, Context ctx) {


        switch (items) {
            case 0:
                return ctx.getString(R.string.never);
            case 1:
                return ctx.getString(R.string.once_a_day);
            case 2:
                return ctx.getString(R.string.twice_a_day);
            case 3:
                return ctx.getString(R.string.tree_times_a_day);
            case 4:
                return ctx.getString(R.string.four_times_a_day);
            default:
                return items + ctx.getString(R.string.times_a_day);
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
        //int TODAY_IN_WEEK = DateTime.now().getDayOfWeek(); // ISO8601: MON = 1, TUE = 2...
        LocalDate today = LocalDate.now();
        // iterate over routine items and filter by day
        List<ScheduleItem> doses = new ArrayList<>();
        for (ScheduleItem scheduleItem : routine.scheduleItems()) {
            DailyScheduleItem dsi = DailyScheduleItem.findByScheduleItem(scheduleItem);
            if (dsi != null) {
                boolean takenToday = dsi.takenToday();
                if (scheduleItem.schedule().enabledForDate(today)) {
                    if (includeTaken || (!includeTaken && !takenToday)) {
                        doses.add(scheduleItem);
                    }
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

    public static String stringifyDays(String[] days, Context ctx) {


        Log.d("DAYS", Arrays.toString(days));
        String dayStr = "";
        for (int i = 0; i < days.length - 1; i++) {
            if (i > 0) {
                dayStr += ", ";
            }
            dayStr += days[i];
        }
        return dayStr + ((days.length > 1 ? " " + ctx.getString(R.string.and) + " " : "") + days[days.length - 1]);
    }

    public static String stringifyDays(boolean[] checkedDays, Context ctx) {

        String[] days = getSelectedDays(checkedDays, ctx);

        if (days.length == 7) {
            return ctx.getString(R.string.every_day);
        } else if (days.length == 0) {
            return ctx.getString(R.string.never);
        }

        String dayStr = "";
        for (int i = 0; i < days.length - 1; i++) {
            if (i > 0) {
                dayStr += ", ";
            }
            dayStr += days[i];
        }
        return dayStr + ((days.length > 1 ? " " + ctx.getString(R.string.and) + " " : "") + days[days.length - 1]);
    }

    public static String[] getSelectedDays(boolean[] days, Context ctx) {

        String[] dayNames = ctx.getResources().getStringArray(R.array.day_names);
        ArrayList<String> sdays = new ArrayList<String>();
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                sdays.add(dayNames[i]);
            }
        }
        return sdays.toArray(new String[sdays.size()]);

    }

    public static String[] dayNames(Context ctx) {
        return ctx.getResources().getStringArray(R.array.day_names);
    }

}
