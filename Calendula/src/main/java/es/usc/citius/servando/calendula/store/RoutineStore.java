package es.usc.citius.servando.calendula.store;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import es.usc.citius.servando.calendula.model.Routine;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class RoutineStore implements IRoutineStore {

    TreeSet<Routine> routines;

    private static final IRoutineStore instance = new RoutineStore();

    public static IRoutineStore getInstance() {
        return instance;
    }

    public RoutineStore() {
        routines = new TreeSet<Routine>();
    }

    @Override
    public void addRoutine(Routine r) {
        routines.add(r);
    }

    @Override
    public Routine getRoutine(LocalTime time) {
        for (Routine r : routines) {
            if (r.getTime().compareTo(time) == 0) {
                return r;
            }
        }
        return null;
    }

    @Override
    public Routine getRoutineByName(String name) {

        if (name == null) {
            return null;
        }

        for (Routine r : routines) {
            if (name.equalsIgnoreCase(r.getName())) {
                return r;
            }
        }
        return null;
    }

    @Override
    public Routine getRoutine(String timeAsString) {
        if (timeAsString == null) {
            return null;
        }

        for (Routine r : routines) {
            if (timeAsString.equalsIgnoreCase(r.getTimeAsString())) {
                return r;
            }
        }
        return null;
    }

    @Override
    public void removeRoutine(Routine r) {
        routines.remove(r);
    }

    @Override
    public boolean existRoutine(DateTime time) {
        for (Routine r : routines) {
            if (r.getTime().compareTo(time.toLocalDate()) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Routine> asList() {
        return new ArrayList<Routine>(routines);
    }

    @Override
    public String[] routineNames() {

        int i = 0;
        String names[] = new String[routines.size()];

        for (Routine r : routines) {
            names[i++] = r.getName();
        }
        return names;
    }

    @Override
    public int size() {
        return routines.size();
    }


}
