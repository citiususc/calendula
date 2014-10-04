package es.usc.citius.servando.calendula.model;

/**
 * Created by castrelo on 4/10/14.
 */
public class DailyScheduleItem {


    public DailyScheduleItem(ScheduleItem scheduleItem) {
        this.scheduleItem = scheduleItem;
    }

    public DailyScheduleItem(ScheduleItem scheduleItem, boolean takenToday) {
        this.scheduleItem = scheduleItem;
        this.takenToday = takenToday;
    }

    private ScheduleItem scheduleItem;

    private boolean takenToday;

    public ScheduleItem getScheduleItem() {
        return scheduleItem;
    }

    public void setScheduleItem(ScheduleItem scheduleItem) {
        this.scheduleItem = scheduleItem;
    }

    public boolean takenToday() {
        return takenToday;
    }

    public void setTakenToday(boolean takenToday) {
        this.takenToday = takenToday;
    }
}
