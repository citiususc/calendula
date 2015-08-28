package es.usc.citius.servando.calendula.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;
import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.scheduling.PickupReminderMgr;

public class CalendarActivity extends ActionBarActivity {

    public static final int ACTION_SHOW_REMINDERS = 1;

    Toolbar toolbar;
    View bottomSheet;
    CalendarPickerView calendar;
    DateTime from;
    DateTime to;

    String df;

    private static DateFormat dtf2 = new SimpleDateFormat("dd/MMM");
    private static PickupInfo.PickupComparator pickupComparator = new PickupInfo.PickupComparator();

    private static Date selectedDate = null;
    private List<PickupInfo> pickupInfos;
    private List<LocalDate> bestDays;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        df = getString(R.string.pickup_date_format);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(new InsetDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp), 20, 20, 20, 20));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_activity_calendar));

        from = DateTime.now().minusMonths(3);
        to = DateTime.now().plusMonths(3);

        bottomSheet = findViewById(R.id.pickup_list_container);
        bottomSheet.setVisibility(View.INVISIBLE);

        setupCalendar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_green_dark));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                calendar.scrollToDate(LocalDate.now().toDate());
            }
        }, 500);

        checkIntent();
    }

    @Override
    protected void onDestroy() {
        selectedDate = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_best_day:
                onBestDaySelected();
                return true;
            default:
                return false;
        }
    }

    private void onBestDaySelected() {

        final List<PickupInfo> urgent = urgentMeds(pickupInfos);
        final LocalDate best = bestDays.isEmpty() ? null : bestDays.get(0);
        final List<PickupInfo> next = best==null ? new ArrayList<PickupInfo>() : medsCanTakeAt(best);

        String msg = null;
        LocalDate today = LocalDate.now();

        Log.d("Calendar", "Urgent: " + urgent.size());
        Log.d("Calendar", "Next: " + next.size());

        boolean allowReminder = false;

        // there are not urgent meds, but there are others to pickup
        if(urgent.isEmpty() && best!=null){

            allowReminder = true;

            Log.d("Calendar", "there are not urgent meds, but there are others to pickup");
            if(next.size() > 1){
                msg = getString(R.string.best_single_day_messge, best.toString(getString(R.string.best_date_format)), next.size()) + "\n\n";
            }else{
                msg = getString(R.string.best_single_day_messge_one_med, best.toString(getString(R.string.best_date_format))) + "\n\n";
            }
            msg = addPcikupList(msg, next);
        }

        // there are urgent meds
        Log.d("Calendar", "there are urgent meds");
        if(!urgent.isEmpty()){
            // and others
            Log.d("Calendar", "and others");
            if(best!=null){

                String bestStr = best.equals(LocalDate.now().plusDays(1))? getString(R.string.calendar_date_tomorrow) : best.toString(getString(R.string.best_date_format));

                // and the others date is near
                Log.d("Calendar", "and the others date is near");
                if(today.plusDays(3).isAfter(best)){
                    List<PickupInfo> all = new ArrayList<>();
                    all.addAll(urgent);
                    all.addAll(next);
                    msg = getString(R.string.best_single_day_messge, bestStr, all.size()) + "\n\n";
                    msg = addPcikupList(msg, all);
                }
                // and the others date is not near
                else{

                    allowReminder = true;

                    Log.d("Calendar", "and the others date is not near");
                    msg = addPcikupList(getString(R.string.pending_meds_msg) + "\n\n", urgent);
                    msg +="\n";
                    if(next.size() > 1){
                        Log.d("Calendar", " size > 1");
                        msg += getString(R.string.best_single_day_messge_after_pending,bestStr, next.size()) + "\n\n";
                    }else{
                        Log.d("Calendar", " size <= 1");
                        msg += getString(R.string.best_single_day_messge_after_pending_one_med, bestStr) + "\n\n";
                    }
                    msg = addPcikupList(msg, next);
                }
            }else{
                Log.d("Calendar", " there are only urgent meds");
                // there are only urgent meds
                msg = addPcikupList(getString(R.string.pending_meds_msg) + "\n\n", urgent);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.best_date_recommendation_title)
                        .setPositiveButton(getString(R.string.driving_warning_gotit), new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        if(allowReminder) {
            builder.setNeutralButton(getString(R.string.best_date_reminder), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    onActivateReminder(best);
                }
            });
        }

        AlertDialog alertDialog = builder.create();

        alertDialog.setMessage(msg);
        alertDialog.show();
    }

    private void onActivateReminder(final LocalDate best) {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle(R.string.best_date_reminder)
                .setSingleChoiceItems(
                        getResources().getStringArray(R.array.calendar_pickup_reminder_values),
                        -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PickupReminderMgr.instance().setCheckPickupsAlarm(CalendarActivity.this, best.minusDays(which + 1));
                                Toast.makeText(CalendarActivity.this,"Recordatorio activado!",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                )
                .create();
        alertDialog.show();
    }

    public String addPcikupList(String msg, List<PickupInfo> pks){
        for (PickupInfo p : pks){
            msg += "    • " + p.medicine().name() + " (" + dtf2.format(p.from().toDate()) + " - " + dtf2.format(p.to().toDate()) + ")\n";
        }
        return msg;
    }

    private void checkIntent() {

        int action = getIntent().getIntExtra("action", -1);
        if (action == ACTION_SHOW_REMINDERS) {
               onBestDaySelected();
        }
    }

    private void setupCalendar() {

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);

        updateDecorators();

        calendar.setCellClickInterceptor(new CalendarPickerView.CellClickInterceptor() {
            @Override
            public boolean onCellClicked(Date date) {
                LocalDate d = LocalDate.fromDateFields(date);
                return onDaySelected(d);
            }
        });

        findViewById(R.id.close_pickup_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBottomSheet();
            }
        });

    }

    private void updatePickups(){
        pickupInfos = DB.pickups().findAll();
        Collections.sort(pickupInfos, pickupComparator);
        bestDays = bestDays(pickupInfos);
    }

    private void updateDecorators(){

        updatePickups();

        final Map<Date, List<PickupInfo>> pickups = new HashMap<>();
        Interval interval = new Interval(from, to);

        for (PickupInfo p : pickupInfos) {
            LocalDate d = p.from();
            if (d != null && interval.contains(d.toDateTimeAtStartOfDay())) {
                Date date = d.toDateTimeAtStartOfDay().toDate();
                if (!pickups.containsKey(date)) {
                    pickups.put(date, new ArrayList<PickupInfo>());
                }
                pickups.get(date).add(p);
            }
        }

        List<CalendarCellDecorator> decorators = new ArrayList<>();
        decorators.add(new PickupCellDecorator(pickups));
        if(!bestDays.isEmpty()){
            bestDays = Arrays.asList(bestDays.get(0));
            decorators.add(new BestDayCellDecorator(bestDays));
        }

        calendar.setDecorators(decorators);
        calendar.init(from.toDate(), to.toDate())
                .setShortWeekdays(getResources().getStringArray(R.array.calendar_weekday_names));
        calendar.invalidateViews();
    }

    public List<PickupInfo> urgentMeds(List<PickupInfo> pickupList){

        List<PickupInfo> urgent = new ArrayList<>();
        Collections.sort(pickupList,pickupComparator);
        LocalDate now = LocalDate.now();
        for (PickupInfo p : pickupList){
            if(!p.taken() && p.from().plusDays(9).isBefore(now)) {
                Log.d("Urgent", p.medicine().name() + ", " + p.from().toString(df) + ", " + p.to().toString(df) + ", " + p.taken());
                urgent.add(p);
            }
        }
        return urgent;
    }


    public List<LocalDate> bestDays(List<PickupInfo> pickupList){
        List<LocalDate> bestDays = new ArrayList<>();
        int bestCount = 0;
        if(pickupList.size() > 0) {
            Collections.sort(pickupList,pickupComparator);

            LocalDate today = LocalDate.now();
            LocalDate first = LocalDate.now(); // compute first
            LocalDate now = LocalDate.now().minusDays(9);

            if(now.getDayOfWeek() == DateTimeConstants.SATURDAY){
                now = now.plusDays(2);
            }else if(now.getDayOfWeek() == DateTimeConstants.SUNDAY){
                now = now.plusDays(1);
            }

            for(PickupInfo p : pickupList){
                if(p.from().isAfter(now) && !p.taken()){
                    first = p.from();
                    break;
                }
            }

            Log.d("Calendar", "BestDayCandidate - First: " + first.toString("dd/MM/yy"));

            List<Long> medIds = new ArrayList<>();
            for(int i = 0; i < 10; i++){
                LocalDate d = first.plusDays(i);
                int count = 0;
                if((d.isAfter(today) && d.getDayOfWeek() < DateTimeConstants.SATURDAY) || (d.isBefore(today) &&  d.getDayOfWeek() < DateTimeConstants.FRIDAY)) {
                    for (PickupInfo p : pickupList) {
                        DateTime iStart = p.from().toDateTimeAtStartOfDay();
                        DateTime iEnd = p.from().plusDays(9).toDateTimeAtStartOfDay();

                        Interval interval = new Interval(iStart, iEnd);
                        if (interval.contains(d.toDateTimeAtStartOfDay()) && !p.taken()) {
                            if(!medIds.contains(p.medicine().getId())) {
                                count++;
                                medIds.add(p.medicine().getId());
                            }
                        }
                        /*else if (!p.taken() && iEnd.isBeforeNow() && p.to().toDateTimeAtStartOfDay().isAfterNow()) {
                            if(!medIds.contains(p.medicine().getId())) {
                                count++;
                                medIds.add(p.medicine().getId());
                            }
                        }*/
                    }
                }

                Log.d("Calendar", "BestDayCandidate: " + d.toString("dd/MM/yy") + ": " + count + " meds");

                if(count > bestCount){
                    bestCount = count;
                    bestDays.clear();
                    bestDays.add(d.toDateTimeAtStartOfDay().isAfterNow() ? d : LocalDate.now().plusDays(1));
                }else if(count == bestCount){
                    bestDays.add(d);
                }
                medIds.clear();
            }

        }
        return  bestDays;
    }

    private List<PickupInfo> medsCanTakeAt(LocalDate d){
        List<PickupInfo> all = pickupInfos;
        Collections.sort(all,pickupComparator);
        List<PickupInfo> canTake = new ArrayList<>();
        List<Long> medIds = new ArrayList<>();

        for (PickupInfo p : all) {
            DateTime iStart = p.from().toDateTimeAtStartOfDay();
            DateTime iEnd = p.from().plusDays(9).toDateTimeAtStartOfDay();
            Interval interval = new Interval(iStart,iEnd);
            if(!p.taken() && interval.contains(d.toDateTimeAtStartOfDay())){
                if(!medIds.contains(p.medicine().getId())) {
                    canTake.add(p);
                    medIds.add(p.medicine().getId());
                }
            }
            /*else if (!p.taken() && iEnd.isBeforeNow() && p.to().toDateTimeAtStartOfDay().isAfterNow()){
                if(!medIds.contains(p.medicine().getId())) {
                    canTake.add(p);
                    medIds.add(p.medicine().getId());
                }
            }*/
        }
        return  canTake;
    }

    private boolean onDaySelected(LocalDate date) {

        selectedDate = date.toDateTimeAtStartOfDay().toDate();
        return showPickupsInfo(date);
    }

    private boolean showPickupsInfo(final LocalDate date) {
        final List<PickupInfo> from = DB.pickups().findByFrom(date, true);
        if(!from.isEmpty()) {

            ((TextView) bottomSheet.findViewById(R.id.bottom_sheet_title)).setText(getResources().getString(R.string.title_pickups_bottom_sheet, date.toString(df)));
            calendar.invalidateViews();

            LayoutInflater i = getLayoutInflater();
            LinearLayout list = (LinearLayout) findViewById(R.id.pickup_list);
            list.removeAllViews();
            showBottomSheet();

            for (final PickupInfo p : from) {

                View v = i.inflate(R.layout.calendar_pickup_list_item, null);
                TextView tv1 = ((TextView) v.findViewById(R.id.textView));
                TextView tv2 = ((TextView) v.findViewById(R.id.textView2));
                String interval = getResources().getString(R.string.pickup_interval, p.to().toString(df));

                if (p.taken()) {
                    interval += " ✔";
                    tv1.setAlpha(0.5f);
                } else {
                    tv1.setAlpha(1f);
                    tv2.setAlpha(1f);
                }

                tv1.setText(p.medicine().name());
                tv2.setText(interval);

                tv1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        p.taken(!p.taken());
                        DB.pickups().save(p);
                        calendar.invalidate();
                        setupCalendar();
                        showPickupsInfo(date);
                    }
                });
                list.addView(v);
            }
            return true;
        }
        return false;
    }

    public void showBottomSheet() {
        if (bottomSheet.getVisibility() != View.VISIBLE)
            bottomSheet.startAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom));
        bottomSheet.setVisibility(View.VISIBLE);
    }

    public void hideBottomSheet() {

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_bottom);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomSheet.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        bottomSheet.startAnimation(anim);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheet.getVisibility() == View.VISIBLE) {
            hideBottomSheet();
        } else {
            super.onBackPressed();
        }
    }

    private static class PickupCellDecorator implements CalendarCellDecorator {


        Map<Date, List<PickupInfo>> pickups;

        public PickupCellDecorator(Map<Date, List<PickupInfo>> pickups) {
            this.pickups = pickups;
        }

        @Override
        public void decorate(final CalendarCellView cellView, Date date) {

            boolean isCurrent = cellView.isCurrentMonth();

            cellView.setAlpha(1f);

            if (pickups != null && isCurrent && pickups.containsKey(date)) {
                List<PickupInfo> pickupInfos = pickups.get(date);
                String dateString = Integer.toString(date.getDate());
                SpannableString string = new SpannableString(dateString); // "\n" + count
                string.setSpan(new RelativeSizeSpan(0.8f), 0, dateString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                cellView.setText(string);
                boolean selected = date.equals(selectedDate);
                cellView.setBackgroundResource(selected ? R.drawable.calendar_day_circle : R.drawable.green_cross);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_highlighted);

                boolean allTaken = true;
                for (PickupInfo pki : pickupInfos) {
                    if (!pki.taken()) {
                        allTaken = false;
                    }
                }

                if (allTaken) {
                    cellView.setAlpha(0.3f);
                }

            } else if (!cellView.isEnabled()) {
                cellView.setBackgroundResource(R.color.white);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_disabled);
            } else if (date.equals(LocalDate.now().toDateTimeAtStartOfDay().toDate())) {
                cellView.setBackgroundResource(R.drawable.calendar_today_selector);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_today);
            } else {
                cellView.setBackgroundResource(R.color.white);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day);
            }

        }

    }

    private static class BestDayCellDecorator implements CalendarCellDecorator {

        private final List<LocalDate> bestDates;

        public BestDayCellDecorator(List<LocalDate> dates){
            this.bestDates = dates;
        }

        @Override
        public void decorate(CalendarCellView calendarCellView, Date date) {
            for(LocalDate bestDate: bestDates){
                if(date.equals(bestDate.toDate())){
                    String dateString = Integer.toString(date.getDate());
                    calendarCellView.setText(dateString + " ✔"); // ★
                }
            }
        }
    }

}
