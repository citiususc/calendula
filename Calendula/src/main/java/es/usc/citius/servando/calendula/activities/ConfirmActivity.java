/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.HomePagerActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.view.ArcTranslateAnimation;

public class ConfirmActivity extends CalendulaActivity {

    private static final int DEFAULT_CHECK_MARGIN = 3;
    private static final String TAG = "ConfirmActivity";

    @BindView(R.id.appbar)
    protected AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar)
    protected CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.myFAB)
    protected FloatingActionButton fab;
    @BindView(R.id.patient_avatar)
    protected ImageView avatar;
    @BindView(R.id.patient_avatar_title)
    protected ImageView avatarTitle;
    @BindView(R.id.check_all_image)
    protected ImageView checkAllImage;
    @BindView(R.id.listView)
    protected RecyclerView listView;
    @BindView(R.id.user_friendly_time)
    protected TextView friendlyTime;
    @BindView(R.id.routines_list_item_hour)
    protected TextView hour;
    @BindView(R.id.routines_list_item_minute)
    protected TextView minute;
    @BindView(R.id.textView3)
    protected TextView takeMedsMessage;
    @BindView(R.id.routine_name)
    protected TextView title;
    @BindView(R.id.routine_name_title)
    protected TextView titleTitle;
    @BindView(R.id.check_overlay)
    protected View checkAllOverlay;
    @BindView(R.id.toolbar_title)
    protected View toolbarTitle;

    private boolean fromNotification = false;
    private boolean isDistant;
    private boolean isInWindow;
    private boolean isRoutine;
    private boolean isToday;
    private boolean stateChanged = false;
    private ConfirmItemAdapter itemAdapter;
    private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/YYYY");
    private DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
    private IconicsDrawable checkedIcon;
    private IconicsDrawable uncheckedIcon;
    private int color;
    private int position = -1;
    private List<DailyScheduleItem> items = new ArrayList<>();
    private LocalDate date;
    private LocalTime time;
    private Patient patient;
    private Routine routine;
    private Schedule schedule;
    private String action;
    private String relativeTime = "";

    /*
     * Returns the intake margin interval
     */
    public Pair<DateTime, DateTime> getCheckMarginInterval(DateTime intakeTime) {

        String checkMarginStr = PreferenceUtils.getString(PreferenceKeys.CONFIRM_CHECK_WINDOW_MARGIN, String.valueOf(DEFAULT_CHECK_MARGIN));

        int checkMargin = Integer.parseInt(checkMarginStr);

        DateTime start = intakeTime.minusMinutes(30);
        DateTime end = intakeTime.plusHours(checkMargin);
        return new Pair<>(start, end);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.confirm, menu);

        MenuItem item = menu.findItem(R.id.action_delay);

        if (!isInWindow) {
            item.setVisible(false);
        } else {
            item.setIcon(new IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_history)
                    .color(Color.WHITE)
                    .sizeDp(24));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (fromNotification) {
                    startActivity(new Intent(this, StartActivity.class));
                    finish();
                } else {
                    supportFinishAfterTransition();
                }
                return true;
            case R.id.action_delay:
                showDelayDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDelayDialog() {
        //TODO allow custom delay values
        final int[] values = this.getResources().getIntArray(R.array.delays_array_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notification_delay)
                .setItems(R.array.delays_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int minutes = values[which];
                        if (isRoutine) {
                            AlarmScheduler.instance().onUserDelayRoutine(routine, date, ConfirmActivity.this, minutes);
                        } else {
                            AlarmScheduler.instance().onUserDelayHourlySchedule(schedule, time, date, ConfirmActivity.this, minutes);
                        }

                        // get cancelled items and uncancel them
                        for (DailyScheduleItem item : items) {
                            if (!item.getTakenToday() && item.getTimeTaken() != null) {
                                item.setTimeTaken(null);
                                DB.dailyScheduleItems().saveAndFireEvent(item);
                            }
                        }

                        String msg = ConfirmActivity.this.getString(R.string.alarm_delayed_message, String.valueOf(minutes));
                        Toast.makeText(ConfirmActivity.this, msg, Toast.LENGTH_SHORT).show();
                        supportFinishAfterTransition();
                    }
                });
        builder.create().show();
    }

    public String getDisplayableDose(double dose, String doseString, Medicine m) {
        return doseString + " " + m.getPresentation().units(getResources(), dose);

    }

    public void showEnsureConfirmDialog(final DialogInterface.OnClickListener listener, boolean uncheck) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DateTime t = date.toDateTime(time);

        String title = t.isAfterNow() ? getString(R.string.intake_not_available) :
                getString(R.string.meds_from) + " " + date.toString("EEEE dd") + " " + getString(R.string.at_time_connector) + " " + time.toString(timeFormatter);

        String msg = t.isAfterNow() ? getString(R.string.confirm_future_intake_warning, relativeTime)
                : uncheck ? getString(R.string.unconfirm_past_intake_warning, relativeTime)
                : getString(R.string.confirm_past_intake_warning);


        builder.setMessage(msg)
                .setCancelable(true)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_history, R.color.black, 36))
                .setTitle(title);

        if (t.isAfterNow()) {
            builder.setNegativeButton(getString(R.string.tutorial_understood), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        } else {
            builder.setPositiveButton(uncheck ? getString(R.string.meds_unconfirm_ok) : getString(R.string.meds_confirm_ok), listener)
                    .setNegativeButton(uncheck ? getString(R.string.meds_unconfirm_cancel) : getString(R.string.meds_confirm_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        }

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        if (fromNotification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }

    void onClickFab() {
        boolean somethingChecked = false;
        for (DailyScheduleItem item : items) {
            if (!item.getTakenToday()) {
                item.setTakenToday(true);
                DB.dailyScheduleItems().saveAndUpdateStock(item, true);
                somethingChecked = true;
            }

        }

        if (somethingChecked) {
            itemAdapter.notifyDataSetChanged();
            stateChanged = true;
            fab.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateAllChecked();
                }
            }, 100);
            onAllChecked();
        } else {
            supportFinishAfterTransition();
        }
    }

    void moveArrowsDown(int duration) {
        checkAllImage.animate()
                .translationY(ScreenUtils.dpToPx(getResources(), 150f))
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void showRippleByApi(int x, int y) {

        int duration = 500;
        int arrowDuration = 400;

        checkAllOverlay.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
            }
        }, duration + 300);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showRipple(x, y, duration);
        } else {
            checkAllOverlay.setVisibility(View.VISIBLE);
            checkAllOverlay.animate().alpha(1).setDuration(duration).start();
        }
        moveArrowsDown(arrowDuration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processIntent();
        setContentView(R.layout.activity_confirm);
        ButterKnife.bind(this);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        isToday = LocalDate.now().equals(date);
        isInWindow = AlarmScheduler.isWithinDefaultMargins(date.toDateTime(time));

        DateTime dt = date.toDateTime(time);
        DateTime now = DateTime.now();
        Pair<DateTime, DateTime> interval = getCheckMarginInterval(dt);
        isDistant = !new Interval(interval.first, interval.second).contains(now);

        color = AvatarMgr.colorsFor(getResources(), patient.getAvatar())[0];
        color = Color.parseColor("#263238");

        setupStatusBar(Color.TRANSPARENT);
        setupToolbar("", Color.TRANSPARENT, Color.WHITE);
        toolbar.setTitleTextColor(Color.WHITE);

        avatar.setImageResource(AvatarMgr.res(patient.getAvatar()));
        avatarTitle.setImageResource(AvatarMgr.res(patient.getAvatar()));
        titleTitle.setText(patient.getName());
        title.setText((isRoutine ? routine.getName() : schedule.toReadableString(this)));
        takeMedsMessage.setText(isInWindow ? getString(R.string.agenda_zoom_meds_time) : getString(R.string.meds_from) + " " + date.toString("EEEE dd"));

        relativeTime = DateUtils.getRelativeTimeSpanString(dt.getMillis(), now.getMillis(), 5 * DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();

        hour.setText(time.toString("kk:"));
        minute.setText(time.toString("mm"));
        friendlyTime.setText(relativeTime.substring(0, 1).toUpperCase() + relativeTime.substring(1));

        if (isDistant) {
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.android_orange_dark)));
        }

        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_check_all)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(0));


        checkAllImage.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_check_all)
                .color(Color.WHITE)
                .sizeDp(100)
                .paddingDp(0));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean somethingToCheck = false;
                for (DailyScheduleItem item : items) {
                    if (!item.getTakenToday()) {
                        somethingToCheck = true;
                        break;
                    }
                }
                if (somethingToCheck) {
                    if (isDistant) {
                        showEnsureConfirmDialog(new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onClickFab();
                            }
                        }, false);
                    } else {
                        onClickFab();
                    }
                } else {
                    Snack.show(getResources().getString(R.string.all_meds_taken), ConfirmActivity.this);
                }
            }
        });


        toolbarLayout.setContentScrimColor(patient.getColor());
        setupListView();

        if ("delay".equals(action)) {
            if (isRoutine && routine != null) {
                ReminderNotification.cancel(this, ReminderNotification.routineNotificationId(routine.getId().intValue()));
            } else if (schedule != null) {
                ReminderNotification.cancel(this, ReminderNotification.scheduleNotificationId(schedule.getId().intValue()));
            }
            showDelayDialog();

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // finish and restart with the new params
        finish();
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "Is distant: " + isDistant + " (" + date.toDateTime(time).toString("dd/MM/YYYY, kk:mm")+")", Toast.LENGTH_LONG).show();
    }

    protected void onDailyAgendaItemCheck(final ImageButton v) {
        int total = items.size();
        int checked = 0;

        for (DailyScheduleItem i : items) {
            if (i.getTakenToday())
                checked++;
        }

        if (checked == total) {
            onAllChecked();
        } else {
            if (isRoutine) {
                AlarmScheduler.instance().onDelayRoutine(routine, date, ConfirmActivity.this);
            } else {
                AlarmScheduler.instance().onDelayHourlySchedule(schedule, time, date, ConfirmActivity.this);
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (stateChanged) {
            CalendulaApp.eventBus().post(new ConfirmStateChangeEvent(position));
        }
        super.onDestroy();
    }

    private void animateAllChecked() {

        int width = appBarLayout.getWidth();
        int middle = width / 2;
        int fabCentered = middle - fab.getWidth() / 2;
        int translationX = (int) fab.getX() - fabCentered;
        int translationY = ScreenUtils.dpToPx(getResources(), 150);

        final int rippleX = middle;
        final int rippleY = (int) (fab.getY() + fab.getHeight() / 2) - translationY;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Animation arcAnimation = new ArcTranslateAnimation(0, -translationX, 0, -translationY);
            arcAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    showRippleByApi(rippleX, rippleY);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            arcAnimation.setInterpolator(new DecelerateInterpolator());
            arcAnimation.setDuration(200);
            arcAnimation.setFillAfter(true);
            fab.startAnimation(arcAnimation);

        } else {
            ViewPropertyAnimator animator = fab.animate().translationX(-translationX).setDuration(300);
            animator.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    showRippleByApi(rippleX, rippleY);
                }
            });
            animator.start();
        }
    }

    private void showRipple(int x, int y, int duration) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            LogUtil.d(TAG, "Ripple x,y [" + x + ", " + y + "]");
            checkAllOverlay.setVisibility(View.INVISIBLE);
            // get the final radius for the clipping circle
            int finalRadius = (int) Math.hypot(checkAllOverlay.getWidth(), checkAllOverlay.getHeight());
            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(checkAllOverlay, x, y, fab.getWidth() / 2, finalRadius);
            anim.setInterpolator(new DecelerateInterpolator());
            // make the view visible and start the animation
            checkAllOverlay.setVisibility(View.VISIBLE);
            anim.setDuration(duration).start();
        }
    }

    private void setupListView() {

        loadItems();
        itemAdapter = new ConfirmItemAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        listView.setLayoutManager(llm);
        listView.setAdapter(itemAdapter);
        listView.setItemAnimator(new DefaultItemAnimator());
    }

    private void processIntent() {
        Intent i = getIntent();
        Long routineId = i.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
        Long scheduleId = i.getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1);
        String dateStr = i.getStringExtra(CalendulaApp.INTENT_EXTRA_DATE);
        String timeStr = i.getStringExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME);


        action = i.getStringExtra(CalendulaApp.INTENT_EXTRA_ACTION);
        position = i.getIntExtra(CalendulaApp.INTENT_EXTRA_POSITION, -1);

        fromNotification = position == -1;

        if (dateStr != null) {
            date = LocalDate.parse(dateStr, dateFormatter);
        } else {
            // this should never happen, but, just in case, redirect to home and show error
            Intent intent = new Intent(this, HomePagerActivity.class);
            intent.putExtra("invalid_notification_error", true);
            startActivity(intent);
            finish();
        }

        LogUtil.d(TAG, timeStr + ", " + dateStr + ", " + routineId + ", " + scheduleId + ", " + date);

        if (routineId != -1) {
            isRoutine = true;
            routine = Routine.findById(routineId);
            time = routine.getTime();
            patient = routine.getPatient();
        } else {
            time = LocalTime.parse(timeStr, timeFormatter);
            schedule = Schedule.findById(scheduleId);
            patient = schedule.patient();
        }
    }

    private void loadItems() {
        if (isRoutine) {
            List<ScheduleItem> rsi = routine.getScheduleItems();
            LogUtil.d(TAG, rsi.size() + " items");
            for (ScheduleItem si : rsi) {
                DailyScheduleItem item = DB.dailyScheduleItems().findByScheduleItemAndDate(si, date);
                if (item != null)
                    items.add(item);
            }
        } else {
            items.add(DB.dailyScheduleItems().findBy(schedule, date, time));
        }

        for (DailyScheduleItem i : items) {
            LogUtil.d(TAG, i != null ? i.toString() : "Null");
        }
    }

    private void onAllChecked() {
        if (isRoutine) {
            AlarmScheduler.instance().onIntakeCompleted(routine, date, this);
        } else {
            AlarmScheduler.instance().onIntakeCompleted(schedule, time, date, this);
        }
    }

    private Drawable getCheckedIcon(int color) {
        if (checkedIcon == null) {
            checkedIcon = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_checkbox_marked_circle_outline) //cmd_checkbox_marked_outline
                    .sizeDp(30)
                    .paddingDp(0)
                    .color(color);
        }
        return checkedIcon;
    }

    private Drawable getUncheckedIcon(int color) {
        if (uncheckedIcon == null) {
            uncheckedIcon = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_checkbox_blank_circle_outline) //cmd_checkbox_blank_outline
                    .sizeDp(30)
                    .paddingDp(0)
                    .color(color);
        }
        return uncheckedIcon;
    }

    public static class ConfirmStateChangeEvent {
        public int position = -1;

        public ConfirmStateChangeEvent(int position) {
            this.position = position;
        }
    }

    class ConfirmItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        ConfirmItemViewHolder h;
        DailyScheduleItem i;
        ScheduleItem si;
        Long sid;
        Schedule s;
        Medicine m;
        Presentation p;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.confirm_activity_list_item, parent, false);
            return new ConfirmItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            h = (ConfirmItemViewHolder) holder;
            i = items.get(position);
            si = i.getScheduleItem();
            sid = i.boundToSchedule() ? i.getSchedule().getId() : si.getSchedule().getId();
            s = DB.schedules().findById(sid);
            m = s.medicine();
            p = m.getPresentation();

            String status = getString(R.string.med_not_taken);
            if (i.getTimeTaken() != null) {
                status = (i.getTakenToday() ? getString(R.string.med_taken_at) : getString(R.string.med_cancelled_at)) + " " + i.getTimeTaken().toString("HH:mm") + "h";
            }

            h.med.setText(m.getName());
            h.dose.setText(getDisplayableDose(i.boundToSchedule() ? s.dose() : si.getDose(), i.boundToSchedule() ? s.displayDose() : si.displayDose(), m));
            h.status.setText(status);
            h.dailyScheduleItem = i;
            updateCheckedStatus();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void updateCheckedStatus() {
            Drawable medDrawable = new IconicsDrawable(ConfirmActivity.this)
                    .icon(p.icon())
                    .color(i.getTakenToday() ? Color.parseColor("#81c784") : Color.parseColor("#11000000"))
                    .sizeDp(36)
                    .paddingDp(0);

            Drawable checkDrawable = i.getTakenToday() ?
                    getCheckedIcon(Color.parseColor("#81c784"))
                    : getUncheckedIcon(Color.parseColor("#11000000"));

            h.check.setImageDrawable(checkDrawable);
            h.icon.setImageDrawable(medDrawable);
        }

        class ConfirmItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.med_item_name)
            TextView med;
            @BindView(R.id.med_item_dose)
            TextView dose;
            @BindView(R.id.med_item_status)
            TextView status;
            @BindView(R.id.check_button)
            ImageButton check;
            @BindView(R.id.imageView)
            ImageView icon;

            DailyScheduleItem dailyScheduleItem;

            public ConfirmItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                check.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                final boolean taken = dailyScheduleItem.getTakenToday();
                if (isDistant) {
                    showEnsureConfirmDialog(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dailyScheduleItem.setTakenToday(!taken);
                            DB.dailyScheduleItems().saveAndUpdateStock(dailyScheduleItem, true);
                            stateChanged = true;
                            onDailyAgendaItemCheck(check);
                            notifyItemChanged(getAdapterPosition());
                        }
                    }, taken);
                } else {

                    dailyScheduleItem.setTakenToday(!taken);
                    DB.dailyScheduleItems().saveAndUpdateStock(dailyScheduleItem, true);
                    stateChanged = true;
                    onDailyAgendaItemCheck(check);
                    notifyItemChanged(getAdapterPosition());
                }


            }
        }
    }


}
