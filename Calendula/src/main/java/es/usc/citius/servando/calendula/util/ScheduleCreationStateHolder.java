package es.usc.citius.servando.calendula.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.model.Routine;

/**
 * Created by joseangel.pineiro on 12/12/13.
 */
public class ScheduleCreationStateHolder {

    private static final ScheduleCreationStateHolder instance = new ScheduleCreationStateHolder();

    private Medicine selectedMed;
    private int selectedScheduleIdx = 0;
    private int timesPerDay = 1;
    private List<Routine> selectedRoutines;
    private boolean[] selectedDays = new boolean[]{true, true, true, true, true, true, true}; // 7 days


    private ScheduleCreationStateHolder() {
        setSelectedRoutines(new ArrayList<Routine>());
    }

    public static ScheduleCreationStateHolder getInstance() {
        return instance;
    }

    public Medicine getSelectedMed() {
        return selectedMed;
    }

    public void setSelectedMed(Medicine selectedMed) {
        this.selectedMed = selectedMed;
    }

    public List<Routine> getSelectedRoutines() {
        return selectedRoutines;
    }

    public void setSelectedRoutines(List<Routine> selectedRoutines) {
        this.selectedRoutines = selectedRoutines;
    }

    public boolean[] getSelectedDays() {
        return selectedDays;
    }

    public void setSelectedDays(boolean[] selectedDays) {
        this.selectedDays = selectedDays;
    }

    public void toggleSelectedDay(int i) {
        getSelectedDays()[i] = !getSelectedDays()[i];
        Log.d("Days", Arrays.toString(getSelectedDays()));
    }

    public int getSelectedScheduleIdx() {
        return selectedScheduleIdx;
    }

    public void setSelectedScheduleIdx(int selectedScheduleIdx) {
        this.selectedScheduleIdx = selectedScheduleIdx;
    }

    public int getTimesPerDay() {
        return timesPerDay;
    }

    public void setTimesPerDay(int timesPerDay) {
        this.timesPerDay = timesPerDay;
    }


    public void clear() {
        setTimesPerDay(1);
        setSelectedRoutines(new ArrayList<Routine>());
        setSelectedMed(null);
        setSelectedScheduleIdx(0);
        selectedDays = new boolean[]{true, true, true, true, true, true, true};
    }
}
