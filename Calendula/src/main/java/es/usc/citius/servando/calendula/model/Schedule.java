package es.usc.citius.servando.calendula.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joseangel.pineiro on 12/17/13.
 */
public class Schedule {

    public static final int MON = 0;
    public static final int TUE = 1;
    public static final int WED = 2;
    public static final int THU = 3;
    public static final int FRI = 4;
    public static final int SAT = 5;
    public static final int SUN = 6;

    // TODO: make builder

    private Medicine medicine;

    private List<ScheduleItem> items;

    private boolean[] days;

    public Schedule() {

        setItems(new ArrayList<ScheduleItem>());
        setDays(new boolean[]{true, true, true, true, true, true, true});
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public void addItem(ScheduleItem item){
        items.add(item);
    }

    public boolean[] getDays() {
        return days;
    }

    /**
     * Checks if this schedule is enabled for an specific day of the week.
     * DAY
     * @param dayOfTheWeek The day to chek, in the ISO8601 format, that
     * is: MON = 1, TUE = 2, WED = 3, and so on.
     * @return
     */
    public boolean enabledForDay(int dayOfTheWeek){
        if(dayOfTheWeek < 1 || dayOfTheWeek > 7)
            throw new IllegalArgumentException("Day of the week must be between 1 and 7");

        return days[dayOfTheWeek-1]; // MON is at "0"
    }

    public void setDays(boolean[] days) {
        this.days = days;
    }


    public static ScheduleBuilder builder(){
        return new ScheduleBuilder();
    }

    public List<ScheduleItem> items() {
        return items;
    }

    public void setItems(List<ScheduleItem> items) {
        this.items = items;
    }

    public static class ScheduleBuilder{

        Schedule s;

        public ScheduleBuilder(){
            s = new Schedule();
            s.setDays(new boolean[]{false, false, false, false, false, false, false});
        }

        public ScheduleBuilder dose(ScheduleItem item){
            s.addItem(item);
            return this;
        }

        public ScheduleBuilder doses(List<ScheduleItem> items){
            s.items.addAll(items);
            return this;
        }

        public ScheduleBuilder med(Medicine med){
            s.setMedicine(med);
            return this;
        }

        public ScheduleBuilder allDays(){
            s.setDays(new boolean[]{true, true, true, true, true, true, true});
            return this;
        }

        public ScheduleBuilder days(int... days){
            s.setDays(new boolean[]{false, false, false, false, false, false, false});
            for (int i = 0; i < days.length; i++) {
                s.days[days[i]]=true;
            }
            return this;
        }

        public ScheduleBuilder days(boolean[] days){
            s.setDays(days);
            return this;
        }

        public Schedule build(){
            // TODO: validate
            return s;
        }














    }

}
