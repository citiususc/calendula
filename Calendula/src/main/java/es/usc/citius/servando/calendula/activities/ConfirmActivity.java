package es.usc.citius.servando.calendula.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
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
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;
import es.usc.citius.servando.calendula.util.AvatarMgr;

public class ConfirmActivity extends CalendulaActivity {


    boolean isRoutine;
    Patient patient;
    Routine routine;
    Schedule schedule;
    LocalTime time;
    List<DailyScheduleItem> items = new ArrayList<>();

    DateTimeFormatter df = DateTimeFormat.forPattern("kk:mm");

    ListView listView;
    ImageView avatar;
    TextView title;
    TextView hour;
    TextView minute;

    ItemAdapter itemAdapter;

    IconicsDrawable unchekedIcon;
    IconicsDrawable chekedIcon;

    FloatingActionButton fab;

    boolean stateChanged = false;
    int position = -1;



    int color;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        processIntent();
        color = AvatarMgr.colorsFor(getResources(), patient.avatar())[0];
        color = Color.parseColor("#263238");
        //color = ScreenUtils.equivalentNoAlpha(color, 0.6f);

        setupStatusBar(Color.TRANSPARENT);
        setupToolbar(patient.name(), Color.TRANSPARENT, Color.WHITE);
        toolbar.setTitleTextColor(Color.WHITE);
        //findViewById(R.id.imageView5).setBackgroundColor(color);
        fab = (FloatingActionButton) findViewById(R.id.myFAB);
        listView = (ListView) findViewById(R.id.listView);
        avatar = (ImageView) findViewById(R.id.patient_avatar);
        title = (TextView) findViewById(R.id.routine_name);
        hour = (TextView) findViewById(R.id.routines_list_item_hour);
        minute = (TextView) findViewById(R.id.routines_list_item_minute);

        avatar.setImageResource(AvatarMgr.res(patient.avatar()));
        title.setText(isRoutine ? routine.name() : schedule.toReadableString(this));

        hour.setText(time.toString("kk:"));
        minute.setText(time.toString("mm"));

        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_check_all)
                .color(Color.BLACK)
                .sizeDp(24)
                .paddingDp(0));

        ((ImageView)findViewById(R.id.clock_icon)).setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_clock)
                .colorRes(R.color.agenda_item_title)
                .sizeDp(65)
                .paddingDp(0));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (DailyScheduleItem item : items) {
                    item.setTakenToday(true);
                    item.save();
                }
                itemAdapter.notifyDataSetChanged();
                stateChanged = true;


                String msg =ConfirmActivity.this.getString(R.string.all_meds_taken);
                Toast.makeText(ConfirmActivity.this, msg, Toast.LENGTH_SHORT).show();
                fab.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        supportFinishAfterTransition();
                    }
                }, 500);
            }
        });

        setupListView();
        if("delay".equals(action)){
            showDelayDialog();
        }

    }

    private void setupListView() {

        AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(200);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 50.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(10);
        set.addAnimation(animation);

        final LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);

        listView.setLayoutAnimation(controller);
        itemAdapter = new ItemAdapter(this, R.layout.daily_view_intake_med, items);
        listView.setAdapter(itemAdapter);
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadItems();
                controller.start();
            }
        }, 500);
    }

    private void processIntent() {
        Intent i = getIntent();
        action = i.getStringExtra("action");
        position = i.getIntExtra("position", -1);
        Long id = i.getLongExtra("routine_id",-1);
        if( id != -1){
            isRoutine = true;
            routine = Routine.findById(id);
            time = routine.time();
            patient = routine.patient();

        }else{
            id = i.getLongExtra("schedule_id", -1);
            String timeStr = i.getStringExtra("schedule_time");
            time = LocalTime.parse(timeStr, df);
            schedule = Schedule.findById(id);
            patient = schedule.patient();

        }
    }

    private void loadItems(){
        if(isRoutine){
            List<ScheduleItem> rsi = ScheduleUtils.getRoutineScheduleItems(routine, true);
            for(ScheduleItem si : rsi){
                items.add(DailyScheduleItem.findByScheduleItem(si));
            }
        }else{
            items.add(DB.dailyScheduleItems().findByScheduleAndTime(schedule,time));
        }
        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.confirm, menu);


        menu.findItem(R.id.action_delay).setIcon(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_history)
                .color(Color.WHITE)
                .sizeDp(24));

//        menu.findItem(R.id.action_cancel).setIcon(new IconicsDrawable(this)
//                .icon(CommunityMaterial.Icon.cmd_alarm_off)
//                .color(Color.BLACK)
//                .sizeDp(24));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_delay:
                showDelayDialog();
                return true;
//            case R.id.action_cancel:
//                if (isRoutine) {
//                    AlarmScheduler.instance().onCancelRoutineNotifications(routine, this);
//                } else {
//                    AlarmScheduler.instance().onCancelHourlyScheduleNotifications(schedule, time, this);
//                }
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showDelayDialog() {
        final int[] values = this.getResources().getIntArray(R.array.delays_array_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notification_delay)
                .setItems(R.array.delays_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int minutes = values[which];
                        if(isRoutine) {
                            AlarmScheduler.instance().onDelayRoutine(routine, ConfirmActivity.this, minutes);
                        }
                        else{
                            AlarmScheduler.instance().onDelayHourlySchedule(schedule, time, ConfirmActivity.this, minutes);
                        }

                        String msg =ConfirmActivity.this.getString(R.string.alarm_delayed_message, minutes);
                        Toast.makeText(ConfirmActivity.this, msg, Toast.LENGTH_SHORT).show();
                        supportFinishAfterTransition();

                    }
                });
        builder.create().show();
    }


    private void onDailyAgendaItemCheck() {

        int total = items.size();
        int checked = 0;

        for(DailyScheduleItem i : items){
            if(i.takenToday())
                checked++;
        }

        if (checked == total) {
            if (isRoutine) {
                AlarmScheduler.instance().onCancelRoutineNotifications(routine, this);
            } else {
                AlarmScheduler.instance().onCancelHourlyScheduleNotifications(schedule, time, this);
            }
        } else {
            if (isRoutine) {
                AlarmScheduler.instance().onDelayRoutine(routine, this);
            } else {
                AlarmScheduler.instance().onDelayHourlySchedule(schedule, time, this);
            }
        }
    }

    private class ItemAdapter extends ArrayAdapter<DailyScheduleItem> {

        LayoutInflater layoutInflater = getLayoutInflater();

        public ItemAdapter(Context context, int layoutResourceId, List<DailyScheduleItem> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final DailyScheduleItem i = items.get(position);

            final ScheduleItem si = i.scheduleItem();
            final Long sid = i.boundToSchedule() ?  i.schedule().getId() : si.schedule().getId();
            final Schedule s = DB.schedules().findById(sid);
            final Medicine m = s.medicine();
            final Presentation p = m.presentation();


            if(convertView == null){
                convertView = layoutInflater.inflate(R.layout.confirm_activity_list_item, null);
            }

            final View cv = convertView;
            final TextView med  = (TextView) convertView.findViewById(R.id.med_item_name);
            final TextView dose = (TextView) convertView.findViewById(R.id.med_item_dose);
            final ImageButton check = (ImageButton) convertView.findViewById(R.id.check_button);
//            final ImageView icon = (ImageView) convertView.findViewById(R.id.imageView);
            //final View ripple = convertView.findViewById(R.id.ripple);

            med.setText(m.name());
            dose.setText(getDisplayableDose(i.boundToSchedule() ?
                    s.displayDose() :
                    si.displayDose(), m));

            updateCheckedState(convertView,i,s, si, m,p,getContext(), false);

            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean taken = i.takenToday();
                    i.setTakenToday(!taken);
                    i.save();
                    updateCheckedState(cv, i, s, si, m, p, getContext(), true);
                    onDailyAgendaItemCheck();
                }
            });

            return convertView;
        }
    }


    private void updateCheckedState(View convertView, DailyScheduleItem i, Schedule s, ScheduleItem si, Medicine m, Presentation p, Context ctx, boolean isUser){

        final View cv = convertView;
        final TextView med  = (TextView) convertView.findViewById(R.id.med_item_name);
        final TextView dose = (TextView) convertView.findViewById(R.id.med_item_dose);
        final ImageButton check = (ImageButton) convertView.findViewById(R.id.check_button);
        final ImageView icon = (ImageView) convertView.findViewById(R.id.imageView);
        //final View ripple = convertView.findViewById(R.id.ripple);
        final int whiteAlpha = Color.parseColor("#aaffffff");

        med.setText(m.name());
        dose.setText(getDisplayableDose(i.boundToSchedule() ?
                s.displayDose() :
                si.displayDose(), m));

        int color = Color.BLACK;
        Drawable medDrawable = null;
        Drawable checkDrawable = null;

        if(i.takenToday()){
            color = Color.parseColor("#81c784");
            checkDrawable = getCheckedIcon(color);
            //ripple.setVisibility(View.VISIBLE);
            medDrawable = new IconicsDrawable(ctx)
                    .icon(p.icon())
                    .color(color)
                    .sizeDp(36)
                    .paddingDp(0);
        }else{
            //ripple.setVisibility(View.INVISIBLE);
            checkDrawable = getUncheckedIcon(Color.parseColor("#11000000"));
            medDrawable = new IconicsDrawable(ctx)
                    .icon(p.icon())
                    .color(Color.parseColor("#e57373"))
                    .sizeDp(36)
                    .paddingDp(0);
        }

//        med.setTextColor(color);
//        dose.setTextColor(color);
        check.setImageDrawable(checkDrawable);
        icon.setImageDrawable(medDrawable);

        if(isUser){
            stateChanged = true;
        }
    }


    private Drawable getCheckedIcon(int color) {
        if(chekedIcon == null) {
            chekedIcon = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_checkbox_marked_circle_outline) //cmd_checkbox_marked_outline
                    .sizeDp(30)
                    .paddingDp(0)
                    .color(color);
        }
        return chekedIcon;
    }

    private Drawable getUncheckedIcon(int color) {
        if (unchekedIcon == null) {
            unchekedIcon = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_checkbox_blank_circle_outline) //cmd_checkbox_blank_outline
                    .sizeDp(30)
                    .paddingDp(0)
                    .color(color);
        }
        return unchekedIcon;
    }

    public String getDisplayableDose(String dose, Medicine m) {
        return dose + " " + m.presentation().units(getResources());

    }


    private void showRipple(int duration, View ripple) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            ripple.setVisibility(View.INVISIBLE);
            // get the center for the clipping circle
            int cy = (ripple.getBottom()) / 2;
            int cx = (ripple.getRight()) - cy;

            // get the final radius for the clipping circle
            int finalRadius = ripple.getWidth();

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(ripple, cx, cy, 0, finalRadius);
            // make the view visible and start the animation
            ripple.setVisibility(View.VISIBLE);
            anim.setDuration(duration).start();
        }
    }

    private void hideRipple(int duration, final View ripple, AnimatorListenerAdapter l) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle

            int cy = (ripple.getBottom()) / 2;
            int cx = (ripple.getRight()) - cy;

            // get the final radius for the clipping circle
            int finalRadius = ripple.getWidth();
            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(ripple, cx, cy, finalRadius, 0);
            // make the view visible and start the animation
            anim.addListener(l);
            anim.setDuration(duration).start();
        }
    }

    @Override
    protected void onDestroy() {

        if(stateChanged && position != -1){
            CalendulaApp.eventBus().post(new ConfirmStateCHangeEvent(position));
        }
        super.onDestroy();
    }

    public static class ConfirmStateCHangeEvent {
        public int position = -1;

        public ConfirmStateCHangeEvent(int position) {
            this.position = position;
        }
    }
}
