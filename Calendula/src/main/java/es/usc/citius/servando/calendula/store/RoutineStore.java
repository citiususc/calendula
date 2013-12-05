package es.usc.citius.servando.calendula.store;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import es.usc.citius.servando.calendula.model.Routine;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class RoutineStore {

    private static final RoutineStore instance = new RoutineStore();
    private HashMap<LocalTime, Routine> routines;

    public RoutineStore() {
        routines = new LinkedHashMap<LocalTime, Routine>();
    }

    public static RoutineStore getInstance() {
        return instance;
    }

    public void addRoutine(Routine r) {
        routines.put(r.getTime(), r);
    }

    public Routine getRoutine(LocalTime time) {
        return routines.get(time);
    }

    public Routine getRoutine(String timeAsString) {
        String[] values = timeAsString.split(":");
        return routines.get(new LocalTime(Integer.valueOf(values[0]), Integer.valueOf(values[1])));
    }

    public void removeRoutine(Routine r) {
        routines.remove(r.getTime());
    }

    public boolean existRoutine(DateTime time) {
        return routines.containsKey(time);
    }

    public List<Routine> asList() {
        return new ArrayList<Routine>(routines.values());
    }

    public int size() {
        return routines.size();
    }

}
