package es.usc.citius.servando.calendula.activities;


import android.content.Intent;
import android.graphics.Color;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;
import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.PickupInfo;

public class CalendarActivity extends ActionBarActivity {

    public static final int ACTION_SHOW_REMINDERS = 1;

    Toolbar toolbar;
    View bottomSheet;
    CalendarPickerView calendar;

    String df;


    private static DateFormat dtf = new SimpleDateFormat("MM/dd/yy");

    private static int white = Color.parseColor("#ffffff");
    private static int grey = Color.parseColor("#ff778088");

    private static Date selectedDate = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        df = getString(R.string.pickup_date_format);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_activity_calendar));
        toolbar.setNavigationIcon(new InsetDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp), 20, 20, 20, 20));

        bottomSheet = findViewById(R.id.pickup_list_container);
        bottomSheet.setVisibility(View.INVISIBLE);

        int action = getIntent().getIntExtra("action", -1);
        if (action == ACTION_SHOW_REMINDERS) {
            final PickupInfo next = DB.pickups().findNext();
            if (next != null) {
                selectedDate = next.from().toDateTimeAtStartOfDay().toDate();
            }
        }

        setupCalendar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
        }

        checkIntent();


    }

    @Override
    protected void onDestroy() {
        selectedDate = null;
        super.onDestroy();
    }

    private void checkIntent() {

        int action = getIntent().getIntExtra("action", -1);
        if (action == ACTION_SHOW_REMINDERS) {

            final PickupInfo next = DB.pickups().findNext();
            if (next != null) {
                onDaySelected(next.from());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        calendar.scrollToDate(next.from().toDate());
                    }
                }, 500);
            }

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    calendar.selectDate(LocalDate.now().toDate(), true);
                }
            }, 500);
        }

    }

    private void setupCalendar() {


        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);

        final Map<Date, List<PickupInfo>> pickups = new HashMap<>();
        DateTime from = DateTime.now().minusMonths(2);
        DateTime to = DateTime.now().plusMonths(3);
        Interval interval = new Interval(from, to);

        for (PickupInfo p : DB.pickups().findAll()) {
            LocalDate d = p.from();
            if (d != null && interval.contains(d.toDateTimeAtStartOfDay())) {
                Date date = d.toDateTimeAtStartOfDay().toDate();
                if (!pickups.containsKey(date)) {
                    pickups.put(date, new ArrayList<PickupInfo>());
                }
                pickups.get(date).add(p);
            }
        }


        for (PickupInfo p : DB.pickups().findAll()) {
            Log.d("Calendar", "PK:" + dtf.format(p.from().toDate()) + ", " + p.medicine().name());
        }

        List<CalendarCellDecorator> decorators = new ArrayList<>();
        decorators.add(new PickupCellDecorator(pickups));

        calendar.setDecorators(decorators);
        calendar.init(from.toDate(), to.toDate())
                .setShortWeekdays(getResources().getStringArray(R.array.calendar_weekday_names));

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

    private boolean onDaySelected(LocalDate date) {
        List<PickupInfo> from = DB.pickups().findByFrom(date, true);
        selectedDate = date.toDateTimeAtStartOfDay().toDate();
        if (date.equals(LocalDate.now())) {
            showNotification();
        }

        if (!from.isEmpty()) {
            ((TextView) bottomSheet.findViewById(R.id.bottom_sheet_title)).setText(getResources().getString(R.string.title_pickups_bottom_sheet, date.toString(df)));
            calendar.invalidateViews();
            showPickupsInfo(from);
            return true;
        }

        return false;
    }

    private void showPickupsInfo(List<PickupInfo> pickups) {
        LayoutInflater i = getLayoutInflater();
        LinearLayout list = (LinearLayout) findViewById(R.id.pickup_list);
        list.removeAllViews();
        showBottomSheet();

        for (PickupInfo p : pickups) {

            View v = i.inflate(R.layout.calendar_pickup_list_item, null);
            TextView tv1 = ((TextView) v.findViewById(R.id.textView));
            TextView tv2 = ((TextView) v.findViewById(R.id.textView2));
            String interval = getResources().getString(R.string.pickup_interval, p.to().toString(df));

            if (p.taken()) {
                interval += " ✔";
                tv1.setAlpha(0.5f);
                //tv2.setAlpha(0.5f);
            } else {
                tv1.setAlpha(1f);
                tv2.setAlpha(1f);
            }

            tv1.setText(p.medicine().name());
            tv2.setText(interval);





            list.addView(v);
        }
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

    public void showBottomSheet() {
        if (bottomSheet.getVisibility() != View.VISIBLE)
            bottomSheet.startAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom));
        bottomSheet.setVisibility(View.VISIBLE);
    }


    private void showNotification() {
        String title = "Remember to pickup your meds";
        String description = "2 prescriptions for next days";
        Intent i = new Intent(this, CalendarActivity.class);
        i.putExtra("action", ACTION_SHOW_REMINDERS);
        PickupNotification.notify(this, title, description, i);
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

            boolean isCurrent = isCurrentMonth(cellView);

            Date today = new Date();
            cellView.setAlpha(1f);

            if (pickups != null && isCurrent && pickups.containsKey(date)) {
                List<PickupInfo> pickupInfos = pickups.get(date);
                String count = "";//"● " + pickupInfos.size() + "";
                String dateString = Integer.toString(date.getDate());
                SpannableString string = new SpannableString(dateString); // "\n" + count
                string.setSpan(new RelativeSizeSpan(0.8f), 0, dateString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                string.setSpan(new RelativeSizeSpan(0.7f), dateString.length(), dateString.length() + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                string.setSpan(new RelativeSizeSpan(0.5f), dateString.length() + 1, dateString.length() + 1 + count.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                cellView.setText(string);
                boolean selected = date.equals(selectedDate);
                cellView.setBackgroundResource(selected ? R.drawable.calendar_day_circle : R.drawable.green_cross);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_highlighted);

                boolean allTaken = true;
                Log.d("Calendar", "- - - - - - Pickups:");
                for (PickupInfo pki : pickupInfos) {
                    Log.d("Calendar", "    " + dtf.format(pki.from().toDate()) + ", taken: " + pki.taken() + ", allTaken: " + allTaken);
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

        boolean isCurrentMonth(CalendarCellView cell) {
            try {
                Field f = cell.getClass().getDeclaredField("isCurrentMonth");
                f.setAccessible(true);
                return (boolean) f.get(cell);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }


}
