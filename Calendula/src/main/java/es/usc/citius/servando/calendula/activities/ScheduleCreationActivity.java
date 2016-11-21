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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.j256.ormlite.misc.TransactionManager;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.ScheduleSummaryFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleTimetableFragment;
import es.usc.citius.servando.calendula.fragments.SelectMedicineListFragment;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.ScheduleHelper;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Snack;

//import es.usc.citius.servando.calendula.fragments.MedicineCreateOrEditFragment;

public class ScheduleCreationActivity extends CalendulaActivity implements ViewPager.OnPageChangeListener {

    public static final String TAG = ScheduleCreationActivity.class.getName();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    Schedule mSchedule;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    long mScheduleId;
    PagerSlidingTabStrip tabs;
    MenuItem removeItem;
    Toolbar toolbar;

    boolean autoStepDone = false;
    int pColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules);

        pColor = DB.patients().getActive(this).color();
        setupToolbar(null, pColor);
        setupStatusBar(pColor);
        subscribeToEvents();
        processIntent();


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        TextView title = ((TextView) findViewById(R.id.textView2));
        title.setBackgroundColor(pColor);
        title.setText(getString(mScheduleId != -1 ? R.string.title_edit_schedule_activity : R.string.title_create_schedule_activity));

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(3);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        tabs.setOnPageChangeListener(this);
        tabs.setAllCaps(true);
        tabs.setShouldExpand(true);
        tabs.setDividerPadding(3);
        tabs.setDividerColor(getResources().getColor(R.color.white_50));
        tabs.setDividerColor(getResources().getColor(R.color.transparent));
        tabs.setIndicatorHeight(ScreenUtils.dpToPx(getResources(), 4));
        tabs.setIndicatorColor(ScreenUtils.equivalentNoAlpha(pColor, 0.8f));
        tabs.setTextColor(Color.parseColor("#222222"));
        tabs.setUnderlineColor(ScreenUtils.equivalentNoAlpha(pColor, 0.5f));
        tabs.setViewPager(mViewPager);

        if (mSchedule != null) {
            mViewPager.setCurrentItem(1);
        }


        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSchedule();
            }
        });


    }

    private void processIntent() {
        mScheduleId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1);
        int scheduleType = getIntent().getIntExtra("scheduleType", -1);
        if (mScheduleId != -1) {
            Schedule s = Schedule.findById(mScheduleId);
            if (s != null) {
                ScheduleHelper.instance().setSelectedMed(s.medicine());
                ScheduleHelper.instance().setTimesPerDay(s.items().size());
                ScheduleHelper.instance().setSelectedScheduleIdx(s.items().size() - 1);
                ScheduleHelper.instance().setScheduleItems(s.items());
                ScheduleHelper.instance().setSchedule(s);
                mSchedule = s;
            } else {
                Snack.show("Schedule not found :(", this);
            }
        } else if (scheduleType != -1) {
            ScheduleHelper.instance().setScheduleType(scheduleType);
        }
    }

    public void createSchedule() {
        try {

            final Schedule s = ScheduleHelper.instance().getSchedule();

            TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // save schedule
                    final Patient patient = DB.patients().getActive(getBaseContext());
                    s.setMedicine(ScheduleHelper.instance().getSelectedMed());
                    s.setPatient(patient);
                    s.setState(Schedule.ScheduleState.ENABLED);
                    s.save();

                    Log.d(TAG, "Saving schedule..." + s.toString());

                    if (!s.repeatsHourly()) {
                        for (ScheduleItem item : ScheduleHelper.instance().getScheduleItems()) {
                            item.setSchedule(s);
                            item.save();
                            Log.d(TAG, "Saving item..." + item.getId());
                            // add to daily schedule
                            DailyAgenda.instance().addItem(patient, item, false);
                        }
                    } else {
                        for (DateTime time : s.hourlyItemsToday()) {
                            LocalTime timeToday = time.toLocalTime();
                            DailyAgenda.instance().addItem(patient, s, timeToday);
                        }
                    }
                    // save and fire event
                    DB.schedules().saveAndFireEvent(s);
                    return null;
                }
            });

            ScheduleHelper.instance().clear();
            AlarmScheduler.instance().onCreateOrUpdateSchedule(s, ScheduleCreationActivity.this);
            Log.d(TAG, "Schedule saved successfully!");
            Snack.show(R.string.schedule_created_message, this);
            CalendulaApp.eventBus().post(PersistenceEvents.SCHEDULE_EVENT);
            finish();
        } catch (Exception e) {
            Snack.show("Error creating schedule", this);
            e.printStackTrace();
        }
    }


    public void updateSchedule() {
        try {
            final Schedule s = mSchedule;

            TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // save schedule

                    s.setMedicine(ScheduleHelper.instance().getSelectedMed());
                    final Patient patient = s.patient();

                    List<Long> routinesTaken = new ArrayList<Long>();

                    if (!s.repeatsHourly()) {
                        // remove days if changed
                        boolean[] days = s.days();
                        for (DailyScheduleItem dsi : DB.dailyScheduleItems().findBySchedule(s)) {
                            if (days[dsi.date().getDayOfWeek() - 1]) {
                                DB.dailyScheduleItems().remove(dsi);
                            }
                        }

                        for (ScheduleItem item : s.items()) {
                            DailyScheduleItem d = DailyScheduleItem.findByScheduleItem(item);
                            // if taken today, add to the list
                            if (d != null && d.takenToday()) {
                                routinesTaken.add(item.routine().getId());
                            }
                            item.deleteCascade();
                        }

                        // save new items
                        for (ScheduleItem i : ScheduleHelper.instance().getScheduleItems()) {

                            ScheduleItem item = new ScheduleItem();
                            item.setDose(i.dose());
                            item.setRoutine(i.routine());
                            item.setSchedule(s);
                            item.save();
                            // add to daily schedule
                            DailyAgenda.instance().addItem(patient, item, routinesTaken.contains(item.routine().getId()));
                        }
                    } else {
                        DB.dailyScheduleItems().removeAllFrom(s);
                        for (DateTime time : s.hourlyItemsToday()) {
                            LocalTime timeToday = time.toLocalTime();
                            DailyAgenda.instance().addItem(patient, s, timeToday);
                        }
                    }

                    // save and fire event
                    DB.schedules().saveAndFireEvent(s);
                    return null;
                }
            });

            ScheduleHelper.instance().clear();
            AlarmScheduler.instance().onCreateOrUpdateSchedule(s, ScheduleCreationActivity.this);
            Log.d(TAG, "Schedule saved successfully!");
            Toast.makeText(this, R.string.schedule_created_message, Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating schedule!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public void saveSchedule() {

        if (!validateBeforeSave()) {
            return;
        }

        if (mSchedule != null) {
            updateSchedule();
        } else {
            createSchedule();
        }
    }


    boolean validateBeforeSave() {

        if (ScheduleHelper.instance().getSelectedMed() == null) {
            mViewPager.setCurrentItem(0);
            showSnackBar(R.string.create_schedule_unselected_med);
            return false;
        }

        for (ScheduleItem i : ScheduleHelper.instance().getScheduleItems()) {
            if (i.routine() == null) {
                mViewPager.setCurrentItem(1);
                showSnackBar(R.string.create_schedule_incomplete_items);
                return false;
            }
        }

        for (ScheduleItem i : ScheduleHelper.instance().getScheduleItems()) {
            if (i.dose() <= 0) {
                mViewPager.setCurrentItem(1);
                showSnackBar(R.string.create_schedule_incomplete_doses);
                return false;
            }
        }

        if (ScheduleHelper.instance().getSchedule().type() == Schedule.SCHEDULE_TYPE_CYCLE && (
                ScheduleHelper.instance().getSchedule().getCycleRest() <= 0
                        || ScheduleHelper.instance().getSchedule().getCycleDays() <= 0)) {
            showSnackBar(R.string.cycle_period_cero_message);
            return false;
        }
        return true;
    }


    private void showSnackBar(int string) {
        SnackbarManager.show(
                Snackbar.with(getApplicationContext())
                        .actionLabel("OK")
                        .actionColor(getResources().getColor(R.color.android_orange_darker))
                        .type(SnackbarType.MULTI_LINE)
                        .textColor(getResources().getColor(R.color.white_80)) // change the text color
                        .color(getResources().getColor(R.color.android_orange_dark)) // change the background color
                        .text(getResources().getString(string)), this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.schedules, menu);
        removeItem = menu.findItem(R.id.action_remove);
        removeItem.setVisible(mScheduleId != -1 ? true : false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                showDeleteConfirmationDialog(mSchedule);
                return true;
            default:
                finish();
                return true;
        }
    }

    public void onMedicineSelected(Medicine m, boolean step) {

        ScheduleHelper.instance().setSelectedMed(m);

        if (!step) {
            autoStepDone = true;
        }

        if (!autoStepDone) {
            autoStepDone = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(1);
                }
            }, 500);
        }

        if (mScheduleId == -1) {
            String titleStart = getString(R.string.title_create_schedule_activity);
            String medName = " (" + m.name() + ")";
            String fullTitle = titleStart + medName;

            SpannableString title = new SpannableString(fullTitle);
            title.setSpan(new RelativeSizeSpan(0.7f), titleStart.length(), titleStart.length() + medName.length(), 0);
            title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white_50)), titleStart.length(), titleStart.length() + medName.length(), 0);
            getSupportActionBar().setTitle(title);
        }
    }

    public void onScheduleTypeSelected() {
        ((ScheduleTimetableFragment) getViewPagerFragment(2)).onTypeSelected();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(2);
            }
        }, 500);
    }

    public void onDoseSelectedWithNoMed() {
        mViewPager.setCurrentItem(0);
        Snack.show(getString(R.string.create_schedule_select_med_before_dose), this);
    }


    void showDeleteConfirmationDialog(final Schedule s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.remove_medicine_message_short), s.medicine().name()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DB.schedules().deleteCascade(s, true);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int page) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    protected void onDestroy() {
        unsubscribeFromEvents();
        ScheduleHelper.instance().clear();
        super.onDestroy();
    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public void onEvent(PersistenceEvents.MedicineAddedEvent event) {
        Log.d("onEvent", event.id + " ----");
        ((SelectMedicineListFragment) getViewPagerFragment(0)).setSelectedMed(event.id);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
//            implements PagerSlidingTabStrip.IconTabProvider
    {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 1) {
                return new ScheduleTimetableFragment();
            } /*else if (position == 1) {
                return new ScheduleTypeFragment();
            } */ else if (position == 0) {
                return new SelectMedicineListFragment();

            } else {
                return new ScheduleSummaryFragment();
            }
        }


        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.medicine);
            } /*else if (position == 1) {
                return getString(R.string.schedule_type);
            }*/ else if (position == 1) {
                return getString(R.string.schedule);
            } else {
                return getString(R.string.summary);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

}
