/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.ical.values.DateValue;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;
import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.RepetitionRule;
import es.usc.citius.servando.calendula.util.ScheduleHelper;

public class SummaryCalendarActivity extends CalendulaActivity {

    public static final String START_DATE_FORMAT = "dd-MM-yyyy";
    DateTimeFormatter fmt = DateTimeFormat.forPattern(START_DATE_FORMAT);
    CalendarPickerView calendar;
    int color;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                finish();
                return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        color = DB.patients().getActive(this).color();
        setContentView(R.layout.activity_summary_calendar);
        int color = DB.patients().getActive(this).color();
        setupToolbar(getString(R.string.title_activity_calendar), color);
        setupStatusBar(color);
        setupCalendar();
    }

    private void setupCalendar() {

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        calendar.setVerticalScrollBarEnabled(false);

        RepetitionRule r;
        String rule = getIntent().getStringExtra("rule");
        String date = getIntent().getStringExtra("start");

        int activeDays = getIntent().getIntExtra("active_days", -1);
        int restDays = getIntent().getIntExtra("rest_days", -1);

        LocalDate from = date != null ? LocalDate.parse(date, fmt) : LocalDate.now();
        LocalDate to = from.plusMonths(6);

        if (rule != null) {
            r = new RepetitionRule(rule);
            List<DateTime> dates = r.occurrencesBetween(from.toDateTimeAtStartOfDay(), to.toDateTimeAtStartOfDay(), from.toDateTimeAtStartOfDay());
            Set<Date> hdates = new HashSet<>();
            for (DateTime d : dates)
                hdates.add(d.toDate());

            List<CalendarCellDecorator> decorators = new ArrayList<>();

            DateValue v = r.iCalRule().getUntil();
            Date start = date != null ? from.toDate() : null;
            Date end = v != null ? new LocalDate(v.year(), v.month(), v.day()).toDate() : null;

            decorators.add(new HighlightDecorator(new ArrayList<>(hdates), start, end, color));
            calendar.setDecorators(decorators);
        } else if (activeDays > 0 && restDays > 0) {

            List<Date> hdates = new ArrayList<>();

            LocalDate d = from.plusDays(0); // copy

            while (d.isBefore(to)) {
                if (ScheduleHelper.cycleEnabledForDate(d, from, activeDays, restDays)) {
                    hdates.add(d.toDate());
                }
                d = d.plusDays(1);
            }

            List<CalendarCellDecorator> decorators = new ArrayList<>();
            //DateValue v = r.iCalRule().getUntil();
            //Date start = date != null ? from.toDate() : null;
            //Date end = v != null ? new LocalDate(v.year(), v.month(), v.day()).toDate() : null;
            decorators.add(new HighlightDecorator(hdates, from.toDate(), to.toDate(), color));
            calendar.setDecorators(decorators);
        }

        calendar.init(from.toDate(), to.toDate())
                .setShortWeekdays(getResources().getStringArray(R.array.calendar_weekday_names));
    }

    private void updateStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey_home));
        }
    }

    private static class HighlightDecorator implements CalendarCellDecorator {

        Date today = LocalDate.now().toDateTimeAtStartOfDay().toDate();

        List<Date> days;
        Date start;
        Date end;
        int color;

        public HighlightDecorator(List<Date> days, Date start, Date end, int color) {
            this.color = color;
            this.days = days;
            this.start = start;
            this.end = end;
        }


        @Override
        public void decorate(final CalendarCellView cellView, Date date) {

            boolean highlight = days.contains(date);

            if (highlight && cellView.isCurrentMonth()) {
                cellView.setBackgroundResource(R.drawable.summary_calendar_day_hightlighted);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_highlighted);
                StateListDrawable sld = (StateListDrawable) cellView.getBackground();
                GradientDrawable shape = (GradientDrawable) sld.getCurrent();
                shape.setColor(Color.parseColor("#dadada"));


            } else if (!cellView.isEnabled()) {
                cellView.setBackgroundResource(R.color.white);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_disabled);
            } else if (date.equals(today)) {
                cellView.setBackgroundResource(R.drawable.calendar_today_selector);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_today);
            } else {
                cellView.setBackgroundResource(R.color.white);
                cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day);
            }

            if (cellView.isCurrentMonth()) {
                if (date.equals(start)) {
                    //cellView.setBackgroundColor(Color.parseColor("#bebebe"));
                    cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_today);
                    cellView.setText("[ " + cellView.getText());
                } else if (date.equals(end)) {
                    //cellView.setBackgroundColor(Color.parseColor("#bebebe"));
                    cellView.setText(cellView.getText() + " ]");
                    cellView.setTextAppearance(cellView.getContext(), R.style.calendar_day_today);
                }
            }
        }
    }
}
