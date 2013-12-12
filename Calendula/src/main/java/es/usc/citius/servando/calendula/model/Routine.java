package es.usc.citius.servando.calendula.model;

import org.joda.time.LocalTime;

/**
 * Represents a time during the day that the users choose to take their medicines
 * <p/>
 * Created by joseangel.pineiro on 12/2/13.
 */
public class Routine implements Comparable<Routine> {

    private LocalTime time;
    private String name;

    public Routine() {
    }

    public Routine(LocalTime time, String name) {
        this.time = time;
        this.name = name;
    }

    /**
     * Routine time during day
     */
    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
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

    public String getTimeAsString() {
        return time != null ? (getTime().getHourOfDay() + ":" + getTime().getMinuteOfHour()) : "";
    }

    /**
     * Compare two routines according with the value of the time property
     * Routines with null time are mayor (force to be at the end of the lists)
     *
     * @param routine Routine to compare with
     * @return
     */
    @Override
    public int compareTo(Routine routine) {

        if (routine.getTime() == null && getTime() == null)
            return 0;
        else if (routine.getTime() == null)
            return -1; // this is minor if the other is null
        else if (time == null)
            return 1; // this is mayor if is null
        else
            return getTime().compareTo(routine.getTime());
    }
}
