package es.usc.citius.servando.calendula.util;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

/**
 * Created by joseangel.pineiro on 12/12/13.
 */
public class ScheduleHelper {

    private static ScheduleHelper instance;

    private List<ScheduleItem> scheduleItems;
    private Schedule schedule;
    private Medicine selectedMed;

    private int selectedScheduleIdx = 0;
    private int timesPerDay = 1;


    private ScheduleHelper() {
        setScheduleItems(new ArrayList<ScheduleItem>());
    }

    public static ScheduleHelper instance() {
        if (instance == null)
            instance = new ScheduleHelper();
        return instance;
    }

    public Medicine getSelectedMed() {
        return selectedMed;
    }

    public void setSelectedMed(Medicine selectedMed) {
        this.selectedMed = selectedMed;
    }

    public List<ScheduleItem> getScheduleItems() {
        return scheduleItems;
    }

    public void setScheduleItems(List<ScheduleItem> scheduleItems) {
        this.scheduleItems = scheduleItems;
    }

    public int getSelectedScheduleIdx() {
        return selectedScheduleIdx;
    }

    public void setSelectedScheduleIdx(int selectedScheduleIdx) {
        this.selectedScheduleIdx = selectedScheduleIdx;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setTimesPerDay(int timesPerDay) {
        this.timesPerDay = timesPerDay;
    }

    public int getTimesPerDay() {
        return timesPerDay;
    }

    public void clear() {
        instance = null;
    }

    @Override
    public String toString() {
        return "ScheduleCreationHelper{" +
                "selectedMed=" + selectedMed.name() +
                ", selectedScheduleIdx=" + selectedScheduleIdx +
                ", timesPerDay=" + timesPerDay +
                ", scheduleItems=" + scheduleItems.size() +
                '}';
    }

}
