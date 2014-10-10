package es.usc.citius.servando.calendula.persistence;

import org.joda.time.LocalTime;

import java.util.Comparator;

/**
 * Created by joseangel.pineiro on 7/16/14.
 */
public class ScheduleItemComparator implements Comparator<ScheduleItem> {

    @Override
    public int compare(ScheduleItem a, ScheduleItem b) {

        Routine routineA = a.routine();
        Routine routineB = b.routine();

        if (routineA == null) {
            return 1;
        } else if (routineB == null) {
            return -1;
        }

        if (routineA.time() == null && routineB.time() == null)
            return 0;
        else if (routineA.time() == null)
            return -1;
        else if (routineB.time() == null)
            return 1;
        else if (routineA.time().equals(LocalTime.MIDNIGHT))
            return 1;
        else
            return routineA.time().compareTo(routineB.time());
    }
}

