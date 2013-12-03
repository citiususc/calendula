package es.usc.citius.servando.calendula.model;

import org.joda.time.DateTime;

/**
 * Represents a time during the day that the users choose to take their medicines
 * <p/>
 * Created by joseangel.pineiro on 12/2/13.
 */
public class Routine {

    private DateTime time;
    private String name;

    public Routine() {
    }

    public Routine(DateTime time, String name) {
        this.time = time;
        this.name = name;
    }

    /**
     * Routine time during day
     */
    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    /**
     * Routine name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
