package es.usc.citius.servando.calendula.util;

import android.content.Context;
import android.util.Log;

import com.google.ical.values.Frequency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;

/**
 * Created by joseangel.pineiro on 12/12/13.
 */
public class ScheduleCreationHelper {

    private static final ScheduleCreationHelper instance = new ScheduleCreationHelper();

    private Medicine selectedMed;
    private int selectedScheduleIdx = 0;
    private int timesPerDay = 1;
    private List<ScheduleItem> scheduleItems;
    private boolean[] selectedDays = new boolean[]{false, false, false, false, false, false, false}; // 7 days
    private int icalInterval;
    private Frequency frequency;
    private int repeatType;
    private String rule;


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

    public void addScheduleItem(ScheduleItem i) {
        scheduleItems.add(i);
    }

    public boolean[] getSelectedDays() {
        return selectedDays;
    }

    public void setSelectedDays(boolean[] selectedDays) {
        this.selectedDays = selectedDays;
    }

    public void toggleSelectedDay(int i) {
        selectedDays[i] = !getSelectedDays()[i];
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

    public int getIcalInterval() {
        return icalInterval;
    }

    public void setIcalInterval(int icalInterval) {
        this.icalInterval = icalInterval;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    @Override
    public String toString() {
        return "ScheduleCreationHelper{" +
                "selectedMed=" + selectedMed.name() +
                ", selectedScheduleIdx=" + selectedScheduleIdx +
                ", timesPerDay=" + timesPerDay +
                ", scheduleItems=" + scheduleItems.size() +
                ", selectedDays=" + Arrays.toString(selectedDays) +
                '}';
    }

    public String[] getDays(Context ctx) {
        ArrayList<String> days = new ArrayList<String>();
        String[] dayNames = ScheduleUtils.dayNames(ctx);
        Log.d("DAYS bool: ", Arrays.toString(selectedDays));

        for (int i = 0; i < selectedDays.length; i++) {
            if (selectedDays[i]) {
                Log.d("DAYS", "Add " + i + "( " + dayNames[i] + ") to list");
                days.add(dayNames[i]);
            }
        }

        String[] d = days.toArray(new String[days.size()]);
        Log.d("DAYS bool 2: ", Arrays.toString(d));
        return d;

    }


    public void clear() {
        setTimesPerDay(1);
        selectedMed = null;
        scheduleItems = new ArrayList<ScheduleItem>();
        setSelectedScheduleIdx(0);
        selectedDays = new boolean[]{true, true, true, true, true, true, true};
        repeatType = 0;
        frequency = Frequency.DAILY;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }
}
