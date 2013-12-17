package es.usc.citius.servando.calendula.model;

import java.util.List;

/**
 * Created by joseangel.pineiro on 12/17/13.
 */
public class Schedule {

    private Medicine medicine;
    private List<Routine> routines;
    private boolean[] days;

    public Schedule() {
        setDays(new boolean[]{true, true, true, true, true, true, true});
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public List<Routine> getRoutines() {
        return routines;
    }

    public void setRoutines(List<Routine> routines) {
        this.routines = routines;
    }

    public boolean[] getDays() {
        return days;
    }

    public void setDays(boolean[] days) {
        this.days = days;
    }
}
