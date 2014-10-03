package es.usc.citius.servando.calendula.model;

import java.util.UUID;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
public class ScheduleItem{

    private String id = UUID.randomUUID().toString();

    private String routineId;
    private Dose dose;

    public ScheduleItem(){
        dose = new Dose();
    }

    public String id(){
        return id;
    }

    public ScheduleItem(String routineId, Dose dose){
        this.routineId = routineId;
        this.dose = dose;
    }


    public String routineId() {
        return routineId;
    }

    public void setRoutine(String routineId) {
        this.routineId = routineId;
    }

    public Dose dose() {
        return dose;
    }

    private void setDose(Dose dose) {
        this.dose = dose;
    }

    @Override
    public String toString() {
        return "ScheduleItem{" +
                "routineId='" + routineId + '\'' +
                ", dose=" + dose.ammount() +
                '}';
    }

}
