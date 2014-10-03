package es.usc.citius.servando.calendula;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;

import java.util.HashMap;
import java.util.Map;

import es.usc.citius.servando.calendula.activities.MessageNotification;
import es.usc.citius.servando.calendula.activities.StartActivity;
import es.usc.citius.servando.calendula.model.Dose;
import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.receiver.AlarmReceiver;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.store.ScheduleStore;
import es.usc.citius.servando.calendula.util.ScheduleUtils;

/**
 * Created by joseangel.pineiro on 7/9/14.
 *
 */
public class AlarmScheduler {

    public static final String INTENT_EXTRA_ROUTINE_ID = "routine_id";
    public static final String INTENT_EXTRA_ACTION = "action";

    public static final int ACTION_ROUTINE_TIME = 1;
    //public static final int ACTION_DAILY_ALARM = 2;

    private static final String TAG = AlarmScheduler.class.getName();

    // TODO: Handle routine time changes!

    /**
     * Set an alarm to an specific routine time using the
     * android AlarmManager service. This alarm will 
     *
     * @param routine The routine whose alarm will be set
     */
    public void setAlarm(Routine routine, Context ctx){

        // set the routine alarm only if there are schedules associated
        if(ScheduleUtils.getRoutineScheduleItems(routine).size() > 0) {
            Log.d(TAG, "Updating routine alarm ["+routine.getName()+"]");
            // intent our receiver will receive
            Intent intent = new Intent(ctx, AlarmReceiver.class);
            // indicate thar is for a routine
            intent.putExtra(INTENT_EXTRA_ACTION, ACTION_ROUTINE_TIME);
            // pass the routine id (hash code)
            intent.putExtra(INTENT_EXTRA_ROUTINE_ID, routine.id());
            // create pending intent
            int intent_id = routine.id().hashCode();
            PendingIntent routinePendingIntent = PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Get the AlarmManager service
            AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            // set the routine alarm, with repetition every 10 minutes
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, routine.getTime().toDateTimeToday().getMillis(), AlarmManager.INTERVAL_DAY, routinePendingIntent);

                Duration timeToAlarm= new Duration(LocalTime.now().toDateTimeToday(), routine.getTime().toDateTimeToday());
                Log.d(TAG, "Alarm scheduled to " + timeToAlarm.getMillis() + " millis");
            }
        }
    }
    
    
    public void cancelAlarm(Routine routine, Context ctx){
        // intent we have sent to set the alarm
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        // indicate thar is for a routine
        intent.putExtra(INTENT_EXTRA_ACTION, ACTION_ROUTINE_TIME);
        // pass the routine id
        intent.putExtra(INTENT_EXTRA_ROUTINE_ID, routine.id());
        // create pending intent
        int intent_id = routine.id().hashCode();
        PendingIntent routinePendingIntent = PendingIntent.getBroadcast(ctx, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager!=null) {
            alarmManager.cancel(routinePendingIntent);
        }
    }


    /**
     * Set the alarms for the routines of an specific schedule
     * if they are not yet settled
     *
     * @param schedule The routine whose alarm will be set
     */
    public void setAlarmsIfNeeded(Schedule schedule, Context ctx){
       Log.d(TAG, "Setting alarm for schedule [" + schedule.getMedicine().getName() +"] with " + schedule.items().size() + " items");
       for(ScheduleItem scheduleItem : schedule.items()){
           Log.d(TAG, "Setting alarm for item [" + RoutineStore.instance().get(scheduleItem.routineId()).getName() + "]");
           setAlarm(RoutineStore.instance().get(scheduleItem.routineId()), ctx);
       }
    }

    /**
     * Set the alarms for the daily routines for the current
     * data. This will be called everyday at 00:00h
     */
    public void scheduleDailyAlarms(Context ctx){
        for(Routine r : RoutineStore.instance().asList()){
            setAlarm(r, ctx);
        }
    }

    /**
     * Cancels the alarms of the daily routines
     */
    public void cancelDailyAlarms(Context ctx){
        for(Routine r : RoutineStore.instance().asList()){
            cancelAlarm(r, ctx);
        }
    }

    /**
     * Called by the when an previously established alarm is received.
     *
     * @param routineId the id of a routine
     */
    public void onAlarmReceived(String routineId, Context ctx){

        Routine routine = RoutineStore.instance().get(routineId);
        if(routine != null) {
            LocalTime now = LocalTime.now();
            LocalTime time = routine.getTime();
            if(time.isBefore(now.minusMinutes(30))){
                // we are receiving an alarm for a routine that is in the past,
                // probably because the application has started just now.
                // We need to log and inform user
                onRoutineLost(routine);
            }else{
                onRoutineTime(routine,ctx);
            }


        }
    }

    /**
     * Called when this class receives an alarm from the AlarmReceiver,
     * and the routine time is after the current time
     * @param routine
     */
    private void onRoutineTime(Routine routine, Context ctx){

        Map<Schedule,ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine);

        // now doses contain the schedule items for the current routine
        // associated with their respective schedules

        for(Schedule s : doses.keySet()){

            Medicine med = s.getMedicine();
            Dose dose = doses.get(s).dose();

            Log.d(TAG, RoutineStore.instance().get(doses.get(s).routineId()).getName() + " - Take " + dose.ammount() + " of " + med.getName());
        }

        final Intent intent = new Intent(ctx, StartActivity.class);
        intent.putExtra("action",StartActivity.ACTION_SHOW_REMINDERS);
        intent.putExtra("routine_id", routine.id());

        MessageNotification.notify(ctx, "Its time to take your meds", doses, 3, intent);

    }

    /**
     * Called when this class receives an alarm from the AlarmReceiver,
     * and the routine time has passed
     * @param routine
     */
    private void onRoutineLost(Routine routine){

        Map<Schedule,ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine);

        // now doses contain the schedule items for the current routine
        // associated with their respective schedules

        for(Schedule s : doses.keySet()){

            Medicine med = s.getMedicine();
            Dose dose = doses.get(s).dose();

            Log.d(TAG, med.getName() + " lost today!");
        }

    }

    // SINGLETON

    private AlarmScheduler(){}
    // static instance
    private static final AlarmScheduler instance = new AlarmScheduler();
    // static method to get the AlarmScheduler instance
    public static AlarmScheduler instance(){return instance;}

}
