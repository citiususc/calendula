package es.usc.citius.servando.calendula.model;

import org.joda.time.LocalTime;

import java.util.Comparator;

import es.usc.citius.servando.calendula.store.RoutineStore;

/**
 * Created by joseangel.pineiro on 7/16/14.
 */
public class ScheduleItemComparator implements Comparator<ScheduleItem> {

    @Override
    public int compare(ScheduleItem a, ScheduleItem b) {

        Routine routineA = RoutineStore.instance().get(a.routineId());
        Routine routineB = RoutineStore.instance().get(b.routineId());

        if(routineA == null ){
            return 1;
        }else if(routineB == null){
            return -1;
        }

        if (routineA.getTime() == null && routineB.getTime() == null)
            return 0;
        else if (routineA.getTime() == null)
            return -1;
        else if (routineB.getTime() == null)
            return 1;
        else if(routineA.getTime().equals(LocalTime.MIDNIGHT))
            return 1;
        else
            return routineA.getTime().compareTo(routineB.getTime());
    }
}

