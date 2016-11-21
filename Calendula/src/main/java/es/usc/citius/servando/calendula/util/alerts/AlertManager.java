package es.usc.citius.servando.calendula.util.alerts;

import android.content.Context;
import android.util.Log;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;

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
                blockRoutinesForMedicine(alert.getMedicine());
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

    private static void blockRoutinesForMedicine(Medicine medicine) {
        // TODO: 21/11/16
    }

}
