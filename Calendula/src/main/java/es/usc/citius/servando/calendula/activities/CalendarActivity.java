package es.usc.citius.servando.calendula.activities;


import android.content.Intent;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PickupInfo;

public class CalendarActivity extends ActionBarActivity {

    public static final int ACTION_SHOW_REMINDERS = 1;

    Toolbar toolbar;
    View bottomSheet;
    CalendarPickerView calendar;

    String df;


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

        setupCalendar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
        }

        checkIntent();


    }

    private void checkIntent() {

        int action = getIntent().getIntExtra("action", -1);
        if (action == ACTION_SHOW_REMINDERS) {

            PickupInfo next = DB.pickups().findNext();
            if (next != null) {
                onDaySelected(next.from());
            }

        }

    }

    private void setupCalendar() {


        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Collection<Date> dates = new HashSet<>();

        DateTime from = DateTime.now().minusMonths(3);
        DateTime to = DateTime.now().plusMonths(6);
        Interval interval = new Interval(from, to);
        for (Medicine m : DB.medicines().findAll()) {
            LocalDate d = m.nextPickupDate();
            if (d != null && interval.contains(d.toDateTimeAtCurrentTime())) {
                dates.add(d.toDate());
            }
        }

        calendar.init(from.toDate(), to.toDate()).withHighlightedDates(dates);
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
        List<PickupInfo> from = DB.pickups().findByFrom(date);

        if (date.equals(LocalDate.now())) {
            showNotification();
        }

        if (!from.isEmpty()) {
            ((TextView) bottomSheet.findViewById(R.id.bottom_sheet_title)).setText(getResources().getString(R.string.title_pickups_bottom_sheet, date.toString(df)));
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

            String interval = getResources().getString(R.string.pickup_interval, p.to().toString(df));

            View v = i.inflate(R.layout.calendar_pickup_list_item, null);
            ((TextView) v.findViewById(R.id.textView)).setText(p.medicine().name());
            ((TextView) v.findViewById(R.id.textView2)).setText(interval);
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

}
