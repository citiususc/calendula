package es.usc.citius.servando.calendula.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
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

import org.joda.time.DateTime;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;

/**
 * Created by joseangel.pineiro on 10/21/14.
 */
public class AgendaZoomHelper {

    Routine r = null;
    LinearLayout list;
    ImageButton doneButton = null;
    Routine routine;
    List<ScheduleItem> doses;
    boolean totalChecked;
    View v;

    boolean somethingChanged = false;
    AnimatorSet animator;

    public AgendaZoomHelper(View v, ZoomHelperListener listener) {
        this.v = v;
        this.mListener = listener;
        list = (LinearLayout) v.findViewById(R.id.reminder_list);
        doneButton = (ImageButton) v.findViewById(R.id.button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

    }


    public void show(Activity activity, View from, Routine r) {
        if (animator == null) {
            routine = r;
            doses = ScheduleUtils.getRoutineScheduleItems(r, true);
            list.removeAllViews();
            ((TextView) v.findViewById(R.id.clock)).setText(r.time().toString("kk:mm"));
            ((TextView) v.findViewById(R.id.routine_name)).setText(r.name() + ", " + doses.size() + " " + activity.getResources().getString(R.string.medicine) + (doses.size() > 1 ? "s" : ""));
            String hr = new PrettyTime().format(r.time().toDateTimeToday().toDate());
            ((TextView) v.findViewById(R.id.meds_time_view)).setText(hr.replaceFirst(hr.charAt(0) + "", (hr.charAt(0) + "").toUpperCase()));
            zoomInView(from, activity);
            if (mListener != null)
                mListener.onShow(r);
        }
    }

    public void remind(Activity activity, Routine r) {
        if (animator == null) {
            routine = r;
            doses = ScheduleUtils.getRoutineScheduleItems(r, true);
            list.removeAllViews();
            ((TextView) v.findViewById(R.id.clock)).setText(DateTime.now().toString("kk:mm"));
            ((TextView) v.findViewById(R.id.routine_name)).setText(r.name());
            ((TextView) v.findViewById(R.id.meds_time_view)).setText("It's time to take some meds!");
            zoomInView(null, activity);
        }
    }


    public void hide() {
        if (somethingChanged) {
            mListener.onChange();
        }
        mListener.onHide();
        zoomOutView();

    }

    private void zoomOutView() {
        TransitionDrawable transition = (TransitionDrawable) v.getBackground();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(View.INVISIBLE);
            }
        }, 250);
        transition.reverseTransition(250);
    }

    private void zoomInView(View from, final Activity activity) {

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        v.setVisibility(View.VISIBLE);
        if (from != null) {
            //animate only for ice cream sandwich and newer versions
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

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
                set.play(ObjectAnimator.ofFloat(v, View.X, startBounds.left, finalBounds.left))
                        .with(ObjectAnimator.ofFloat(v, View.Y, startBounds.top, finalBounds.top))
                                //                .with(ObjectAnimator.ofFloat(v, View.SCALE_X,startScale, 1f))
                        .with(ObjectAnimator.ofFloat(v,
                                View.SCALE_Y, startScale, 1f));
                set.setDuration(250);
                set.setStartDelay(0);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onAfterShow(activity);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }
                });
                animator = set;
                set.start();
            }
        } else {
            onAfterShow(activity);
        }

    }

    void onAfterShow(Activity activity) {
        v.findViewById(R.id.content).setVisibility(View.VISIBLE);
        fillReminderList(activity);
        list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(activity, R.anim.reminder_list_controller));
        list.getLayoutAnimation().start();
        TransitionDrawable transition = (TransitionDrawable) v.getBackground();
        transition.startTransition(250);
        animator = null;
    }


    public String getDisplayableDose(Activity activity, String dose, Medicine m, Routine r) {
        return dose
                + " "
                + m.presentation().units(activity.getResources())
                + " - "
                + r.time().toString("kk:mm")
                + "h";
    }


    void fillReminderList(final Activity activity) {

        LayoutInflater inflater = activity.getLayoutInflater();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 15);

        for (ScheduleItem scheduleItem : doses) {
            final Routine r = scheduleItem.routine();
            final Medicine med = scheduleItem.schedule().medicine();
            final DailyScheduleItem dsi = DailyScheduleItem.findByScheduleItem(scheduleItem);
            final View entry = inflater.inflate(R.layout.reminder_item, null);
            final View background = entry.findViewById(R.id.reminder_item_container);
            final ToggleButton checkButton = (ToggleButton) entry.findViewById(R.id.check_button);

            ((TextView) entry.findViewById(R.id.med_item_name)).setText(med.name());
            ((ImageView) entry.findViewById(R.id.imageView)).setImageResource(med.presentation().getDrawable());
            ((TextView) entry.findViewById(R.id.med_item_dose)).setText(getDisplayableDose(activity, scheduleItem.displayDose(), med, r));

            entry.setTag(dsi);

            checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    background.setSelected(checked);
                    DailyScheduleItem dailyScheduleItem = (DailyScheduleItem) entry.getTag();
                    dailyScheduleItem.setTakenToday(checked);
                    dailyScheduleItem.save();
                    somethingChanged = true;
                    onReminderChecked(activity);
                    Log.d("Detail", dailyScheduleItem.scheduleItem().schedule().medicine().name() + " taken: " + checked);
                }
            });

            entry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkButton.performClick();
                }
            });

            if (dsi.takenToday()) {
                checkButton.setChecked(true);
                background.setSelected(true);
            }

            list.addView(entry, params);
        }
    }

    private void onReminderChecked(Activity activity) {

        int total = doses.size();
        int checked = 0;

        for (ScheduleItem s : doses) {
            boolean taken = DailyScheduleItem.findByScheduleItem(s).takenToday();
            if (taken)
                checked++;
        }

        if (checked == total) {
            totalChecked = true;
            AlarmScheduler.instance().onCancelRoutineNotifications(routine, activity);
        } else {
            AlarmScheduler.instance().onDelayRoutine(routine, activity);
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

