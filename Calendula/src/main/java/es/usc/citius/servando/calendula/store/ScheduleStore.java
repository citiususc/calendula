package es.usc.citius.servando.calendula.store;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class ScheduleStore {

    TreeSet<Schedule> schedules;

    private static final ScheduleStore instance = new ScheduleStore();

    public static ScheduleStore getInstance() {
        return instance;
    }

    public ScheduleStore() {
        schedules = new TreeSet<Schedule>();
    }


    public void addSchedule(Schedule r) {
        schedules.add(r);
    }

    public void removeSchedule(Routine r) {
        schedules.remove(r);
    }

    public List<Schedule> asList() {
        return new ArrayList<Schedule>(schedules);
    }

    public int size() {
        return schedules.size();
    }


}
