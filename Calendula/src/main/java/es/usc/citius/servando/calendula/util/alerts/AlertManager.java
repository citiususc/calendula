package es.usc.citius.servando.calendula.util.alerts;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

import static es.usc.citius.servando.calendula.persistence.PatientAlert.Level;

/**
 * Created by alvaro.brey.vilas on 21/11/16.
 */

public class AlertManager {

    private static final String TAG = "AlertManager";

    public static void createAlert(final PatientAlert alert, final Context ctx) {
        Log.d(TAG, "createAlert() called with: alert = [" + alert + "], ctx = [" + ctx + "]");
        DB.alerts().save(alert);

        switch (alert.getLevel()) {
            case Level.HIGH:
                blockSchedulesForMedicine(alert.getMedicine());
            case Level.MEDIUM:
                //nothing
            case Level.LOW:
                //nothing
            default:
        }
        CalendulaApp.eventBus().post(PersistenceEvents.ALERT_EVENT);

    }

    private static void blockSchedulesForMedicine(final Medicine medicine) {

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final List<Schedule> schedules = DB.schedules().findByMedicine(medicine);
                Log.d(TAG, "blockSchedulesForMedicine: Found " + schedules.size() + " schedules to block.");
                for (Schedule schedule : schedules) {
                    blockSchedule(schedule);
                }
                return null;
            }
        });

    }

    private static void blockSchedule(final Schedule schedule) {
        schedule.setState(Schedule.ScheduleState.BLOCKED);
        schedule.save();
        for (ScheduleItem i : schedule.items()) {
            DB.dailyScheduleItems().removeAllFrom(i);
        }
        DB.dailyScheduleItems().removeAllFrom(schedule);
        CalendulaApp.eventBus().post(PersistenceEvents.SCHEDULE_EVENT);
    }

    public static void removeAlert(final PatientAlert alert) {

        DB.alerts().remove(alert);
        //if alertlevel was high, check if we need to unblock schedules
        if (alert.getLevel() == Level.HIGH) {

            final List<Schedule> blocked = DB.schedules()
                    .findByMedicineAndState(alert.getMedicine(), Schedule.ScheduleState.BLOCKED);
            if (blocked.size() > 0) {
                final List<PatientAlert> alerts = DB.alerts()
                        .findByMedicineAndLevel(alert.getMedicine(), Level.HIGH);
                if (alerts.size() == 0) {
                    DB.transaction(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            for (Schedule schedule : blocked) {
                                schedule.setState(Schedule.ScheduleState.ENABLED);
                                schedule.save();
                            }
                            return null;
                        }
                    });
                }
            }

        }
    }

}
