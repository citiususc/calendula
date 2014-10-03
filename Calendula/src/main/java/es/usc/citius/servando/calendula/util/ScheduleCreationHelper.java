package es.usc.citius.servando.calendula.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.model.ScheduleItem;

/**
 * Created by joseangel.pineiro on 12/12/13.
 */
public class ScheduleCreationHelper {

    private static final ScheduleCreationHelper instance = new ScheduleCreationHelper();

    private Medicine selectedMed;
    private int selectedScheduleIdx = 0;
    private int timesPerDay = 1;
    private List<ScheduleItem> scheduleItems;
    private boolean[] selectedDays = new boolean[]{true, true, true, true, true, true, true}; // 7 days

    private String[] dayNames = new String[]{"Mon", "Tue", "Wed", "Thu", "Fry", "Sat", "Sun"}; // 7 days

    private ScheduleCreationHelper() {
        setScheduleItems(new ArrayList<ScheduleItem>());
    }

    public static ScheduleCreationHelper instance() {
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

    public void addScheduleItem(ScheduleItem i){
        scheduleItems.add(i);
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

    @Override
    public String toString() {
        return "ScheduleCreationHelper{" +
                "selectedMed=" + selectedMed.getName() +
                ", selectedScheduleIdx=" + selectedScheduleIdx +
                ", timesPerDay=" + timesPerDay +
                ", scheduleItems=" + scheduleItems.size() +
                ", selectedDays=" + Arrays.toString(selectedDays) +
                '}';
    }

    public String[] getDays(){
        ArrayList<String> days = new ArrayList<String>();

        for(int i = 0; i < selectedDays.length;i++){
            if(selectedDays[i]){
                days.add(dayNames[i]);
            }
        }

        return days.toArray(new String[days.size()]);

    }



    public void clear() {
        setTimesPerDay(1);
        scheduleItems = new ArrayList<ScheduleItem>();
        setSelectedMed(null);
        setSelectedScheduleIdx(0);
        selectedDays = new boolean[]{true, true, true, true, true, true, true};
    }
}
