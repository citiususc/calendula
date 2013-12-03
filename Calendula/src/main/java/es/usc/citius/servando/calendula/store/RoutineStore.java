package es.usc.citius.servando.calendula.store;

import org.joda.time.DateTime;

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
    private HashMap<DateTime, Routine> routines;

    public RoutineStore() {
        routines = new LinkedHashMap<DateTime, Routine>();
    }

    public static RoutineStore getInstance() {
        return instance;
    }

    public void addRoutine(Routine r) {
        routines.put(r.getTime(), r);
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

}
