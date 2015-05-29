package es.usc.citius.servando.calendula.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.ocpsoft.pretty.time.PrettyTime;
import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;
import es.usc.citius.servando.calendula.util.AppTutorial;
import es.usc.citius.servando.calendula.util.Snack;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * Created by joseangel.pineiro on 10/21/14.
 */
public class AgendaZoomHelper {

    LinearLayout list;
    ImageButton doneButton = null;
    ImageButton delayButton = null;
    Routine routine;
    Schedule schedule;
    LocalTime time;
    List<ScheduleItem> doses;
    boolean totalChecked;
    View v;

    boolean somethingChanged = false;
    AnimatorSet animator;
    Activity activity;

    public AgendaZoomHelper(View v, Activity activity, ZoomHelperListener listener)
    {
        this.v = v;
        this.activity = activity;
        this.mListener = listener;
        list = (LinearLayout) v.findViewById(R.id.reminder_list);
        doneButton = (ImageButton) v.findViewById(R.id.button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                hide();
            }
        });

        delayButton = (ImageButton) v.findViewById(R.id.delay_button);
        delayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (routine != null)
                {
                    showDelayDialog(routine);
                } else if (schedule != null) showDelayDialog(schedule, time);
            }
        });
    }

    public void showDelayDialog(final Routine routineToDelay)
    {
        final int[] values = activity.getResources().getIntArray(R.array.delays_array_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.notification_delay)
            .setItems(R.array.delays_array, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    int minutes = values[which];
                    AlarmScheduler.instance().onDelayRoutine(routineToDelay, activity, minutes);
                    Snack.show(activity.getString(R.string.alarm_delayed_message, minutes),
                        activity);
                }
            });
        builder.create().show();
    }

    public void showDelayDialog(final Schedule schedule, final LocalTime time)
    {
        final int[] values = activity.getResources().getIntArray(R.array.delays_array_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.notification_delay)
            .setItems(R.array.delays_array, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    int minutes = values[which];
                    AlarmScheduler.instance()
                        .onDelayHourlySchedule(schedule, time, activity, minutes);
                    Snack.show(activity.getString(R.string.alarm_delayed_message, minutes),
                        activity);
                }
            });
        builder.create().show();
    }

    public void show(Activity activity, View from, Routine r)
    {
        if (animator == null)
        {

            if (AlarmScheduler.instance().isWithinDefaultMargins(r, activity))
            {
                delayButton.setVisibility(View.VISIBLE);
            } else
            {
                delayButton.setVisibility(View.INVISIBLE);
            }

            routine = r;
            doses = ScheduleUtils.getRoutineScheduleItems(r, true);
            list.removeAllViews();
            ((TextView) v.findViewById(R.id.clock)).setText(r.time().toString("kk:mm"));
            ((TextView) v.findViewById(R.id.routine_name)).setText(
                r.name() + ", " + doses.size() + " " + activity.getResources()
                    .getString(R.string.medicine) + (doses.size() > 1 ? "s" : ""));
            String hr = new PrettyTime().format(r.time().toDateTimeToday().toDate());
            ((TextView) v.findViewById(R.id.meds_time_view)).setText(
                hr.replaceFirst(hr.charAt(0) + "", (hr.charAt(0) + "").toUpperCase()));
            zoomInView(from, activity);
            if (mListener != null) mListener.onShow(r);
        }
    }

    public void show(Activity activity, View from, Schedule s, LocalTime time)
    {
        if (animator == null)
        {

            if (AlarmScheduler.instance().isWithinDefaultMargins(time.toDateTimeToday(), activity))
            {
                delayButton.setVisibility(View.VISIBLE);
            } else
            {
                delayButton.setVisibility(View.INVISIBLE);
            }

            routine = null;
            doses = null;
            this.schedule = s;
            this.time = time;

            list.removeAllViews();
            ((TextView) v.findViewById(R.id.clock)).setText(time.toString("kk:mm"));
            ((TextView) v.findViewById(R.id.routine_name)).setText(
                activity.getString(R.string.every).toLowerCase()
                    + " "
                    + s.rule().interval()
                    + " "
                    + activity.getString(R.string.hours));
            String hr = new PrettyTime().format(time.toDateTimeToday().toDate());
            ((TextView) v.findViewById(R.id.meds_time_view)).setText(
                hr.replaceFirst(hr.charAt(0) + "", (hr.charAt(0) + "").toUpperCase()));
            zoomInView(from, activity);
            if (mListener != null)
            {
                // mListener.onShow(r); TODO
            }
        }
    }

    public void remind(Activity activity, Routine r)
    {
        if (animator == null)
        {

            if (AlarmScheduler.instance().isWithinDefaultMargins(r, activity))
            {
                delayButton.setVisibility(View.VISIBLE);
            } else
            {
                delayButton.setVisibility(View.INVISIBLE);
            }

            routine = r;
            doses = ScheduleUtils.getRoutineScheduleItems(r, true);
            list.removeAllViews();
            ((TextView) v.findViewById(R.id.clock)).setText(DateTime.now().toString("kk:mm"));
            ((TextView) v.findViewById(R.id.routine_name)).setText(r.name());
            ((TextView) v.findViewById(R.id.meds_time_view)).setText(
                activity.getString(R.string.agenda_zoom_meds_time));
            zoomInView(null, activity);
        }
    }

    public void remind(Activity activity, Schedule s, LocalTime time)
    {
        if (animator == null)
        {

            if (AlarmScheduler.instance().isWithinDefaultMargins(time.toDateTimeToday(), activity))
            {
                delayButton.setVisibility(View.VISIBLE);
            } else
            {
                delayButton.setVisibility(View.INVISIBLE);
            }

            routine = null;
            doses = null;
            this.schedule = s;
            this.time = time;
            list.removeAllViews();
            ((TextView) v.findViewById(R.id.clock)).setText(DateTime.now().toString("kk:mm"));
            ((TextView) v.findViewById(R.id.routine_name)).setText(
                activity.getString(R.string.every).toLowerCase()
                    + " "
                    + s.rule().interval()
                    + " "
                    + activity.getString(R.string.hours));
            ((TextView) v.findViewById(R.id.meds_time_view)).setText(
                activity.getString(R.string.agenda_zoom_meds_time));
            zoomInView(null, activity);
        }
    }

    public void hide()
    {
        if (somethingChanged)
        {
            mListener.onChange();
        }
        mListener.onHide();
        zoomOutView();
    }

    private void zoomOutView()
    {
        TransitionDrawable transition = (TransitionDrawable) v.getBackground();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                v.setVisibility(View.INVISIBLE);
            }
        }, 250);
        transition.reverseTransition(250);
    }

    private void zoomInView(View from, final Activity activity)
    {

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        v.setVisibility(View.VISIBLE);
        if (from != null)
        {
            //animate only for ice cream sandwich and newer versions
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            {

                from.getGlobalVisibleRect(startBounds);
                v.getGlobalVisibleRect(finalBounds, globalOffset);
                startBounds.offset(-globalOffset.x, -globalOffset.y);
                finalBounds.offset(-globalOffset.x, -globalOffset.y);

                float startScale = startBounds.height() / finalBounds.height();
                float startHeight = startScale * finalBounds.height();
                float deltaHeight = (startHeight - startBounds.height()) / 2;
                startBounds.top -= deltaHeight;
                startBounds.bottom += deltaHeight;

                v.setPivotX(0);
                v.setPivotY(startBounds.exactCenterY());
                // Construct and run the parallel animation of the four translation and
                // scale properties (X, Y, SCALE_X, and SCALE_Y).
                AnimatorSet set = new AnimatorSet();
                set.play(
                    ObjectAnimator.ofFloat(v, View.X, startBounds.left, finalBounds.left)).with(
                    ObjectAnimator.ofFloat(v, View.Y, startBounds.top, finalBounds.top))
                    //                .with(ObjectAnimator.ofFloat(v, View.SCALE_X,startScale, 1f))
                    .with(ObjectAnimator.ofFloat(v, View.SCALE_Y, startScale, 1f));
                set.setDuration(250);
                set.setStartDelay(0);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        onAfterShow(activity);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {

                    }
                });
                animator = set;
                set.start();
            }
        } else
        {
            onAfterShow(activity);
        }
    }

    void onAfterShow(Activity activity)
    {
        v.findViewById(R.id.content).setVisibility(View.VISIBLE);
        if (routine != null)
        {
            fillReminderList(activity);
        } else
        {
            fillReminderListForSchedule(activity);
        }
        list.setLayoutAnimation(
            AnimationUtils.loadLayoutAnimation(activity, R.anim.reminder_list_controller));
        list.getLayoutAnimation().start();
        TransitionDrawable transition = (TransitionDrawable) v.getBackground();
        transition.startTransition(250);

        if (v.findViewById(R.id.check_button) != null)
        {
            Log.d("AgendaZoomHelper", "Show tutorial if needed...");
            ((HomeActivity) activity).getTutorial()
                .show(AppTutorial.NOTIFICATION_INFO, R.id.check_button, activity);
        }
        animator = null;
    }

    public String getDisplayableDose(Activity activity, String dose, Medicine m)
    {
        return dose + " " + m.presentation().units(activity.getResources());
        //                + " - "
        //                + r.time().toString("kk:mm")
        //                + "h";
    }

    void fillReminderList(final Activity activity)
    {

        LayoutInflater inflater = activity.getLayoutInflater();

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, 0, 0, 15);

        for (ScheduleItem scheduleItem : doses)
        {
            final Routine r = scheduleItem.routine();
            final Medicine med = scheduleItem.schedule().medicine();
            final DailyScheduleItem dsi = DailyScheduleItem.findByScheduleItem(scheduleItem);
            final View entry = inflater.inflate(R.layout.reminder_item, null);
            final View background = entry.findViewById(R.id.reminder_item_container);
            final ToggleButton checkButton = (ToggleButton) entry.findViewById(R.id.check_button);

            ((TextView) entry.findViewById(R.id.med_item_name)).setText(med.name());
            ((ImageView) entry.findViewById(R.id.imageView)).setImageResource(
                med.presentation().getDrawable());
            ((TextView) entry.findViewById(R.id.med_item_dose)).setText(
                getDisplayableDose(activity, scheduleItem.displayDose(), med));

            entry.setTag(dsi);

            checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
                {
                    background.setSelected(checked);
                    DailyScheduleItem dailyScheduleItem = (DailyScheduleItem) entry.getTag();
                    dailyScheduleItem.setTakenToday(checked);
                    dailyScheduleItem.save();
                    somethingChanged = true;
                    onReminderChecked(activity);
                    Log.d("Detail", dailyScheduleItem.scheduleItem().schedule().medicine().name()
                        + " taken: "
                        + checked);
                }
            });

            entry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    checkButton.performClick();
                }
            });

            if (dsi.takenToday())
            {
                checkButton.setChecked(true);
                background.setSelected(true);
            }

            list.addView(entry, params);
        }
    }

    void fillReminderListForSchedule(final Activity activity)
    {

        LayoutInflater inflater = activity.getLayoutInflater();

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, 0, 0, 15);

        //final Routine r = scheduleItem.routine();
        final Medicine med = schedule.medicine();
        final DailyScheduleItem dsi = DB.dailyScheduleItems().findByScheduleAndTime(schedule, time);
        final View entry = inflater.inflate(R.layout.reminder_item, null);
        final View background = entry.findViewById(R.id.reminder_item_container);
        final ToggleButton checkButton = (ToggleButton) entry.findViewById(R.id.check_button);

        ((TextView) entry.findViewById(R.id.med_item_name)).setText(med.name());
        ((ImageView) entry.findViewById(R.id.imageView)).setImageResource(
            med.presentation().getDrawable());
        ((TextView) entry.findViewById(R.id.med_item_dose)).setText(
            getDisplayableDose(activity, schedule.displayDose(), med));

        entry.setTag(dsi);

        checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
            {
                background.setSelected(checked);
                DailyScheduleItem dailyScheduleItem = (DailyScheduleItem) entry.getTag();
                dailyScheduleItem.setTakenToday(checked);
                dailyScheduleItem.save();
                somethingChanged = true;
                onReminderChecked(activity);
                //Log.d("Detail", dailyScheduleItem.scheduleItem().schedule().medicine().name() + " taken: " + checked);
            }
        });

        entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                checkButton.performClick();
            }
        });

        if (dsi.takenToday())
        {
            checkButton.setChecked(true);
            background.setSelected(true);
        }

        list.addView(entry, params);
    }

    private void onReminderChecked(Activity activity)
    {

        int total = routine != null ? doses.size() : 1;
        int checked = 0;

        if (routine != null)
        {
            for (ScheduleItem s : doses)
            {
                boolean taken = DailyScheduleItem.findByScheduleItem(s).takenToday();
                if (taken) checked++;
            }
        } else
        {
            boolean taken =
                DB.dailyScheduleItems().findByScheduleAndTime(schedule, time).takenToday();
            if (taken) checked++;
        }

        if (checked == total)
        {
            totalChecked = true;
            if (routine != null)
            {
                AlarmScheduler.instance().onCancelRoutineNotifications(routine, activity);
            } else
            {
                AlarmScheduler.instance()
                    .onCancelHourlyScheduleNotifications(schedule, time, activity);
            }
        } else
        {
            if (routine != null)
            {
                AlarmScheduler.instance().onDelayRoutine(routine, activity);
            } else
            {
                AlarmScheduler.instance().onDelayHourlySchedule(schedule, time, activity);
            }
            totalChecked = false;
        }
    }

    public interface ZoomHelperListener {
        void onChange();

        void onHide();

        void onShow(Routine r);
    }

    public ZoomHelperListener mListener;
}

