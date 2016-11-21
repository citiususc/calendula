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
                if (ctx != null) {
                    alert.showDialog(ctx);
                }
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
                for (Schedule schedule : schedules) {
                    schedule.setState(Schedule.ScheduleState.BLOCKED);
                    schedule.save();
                }
                return null;
            }
        });

    }

}
