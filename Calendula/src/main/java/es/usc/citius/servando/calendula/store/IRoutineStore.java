package es.usc.citius.servando.calendula.store;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.List;

import es.usc.citius.servando.calendula.model.Routine;

/**
 * Created by joseangel.pineiro on 12/12/13.
 */
public interface IRoutineStore {
    void addRoutine(Routine r);

    Routine getRoutine(LocalTime time);

    Routine getRoutineByName(String name);

    Routine getRoutine(String timeAsString);

    void removeRoutine(Routine r);

    boolean existRoutine(DateTime time);

    List<Routine> asList();

    String[] routineNames();

    int size();
}
