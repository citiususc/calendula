package es.usc.citius.servando.calendula.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.ScheduleSummaryFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleTimetableFragment;
import es.usc.citius.servando.calendula.fragments.SelectMedicineListFragment;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Persistence;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.ScheduleCreationHelper;
import es.usc.citius.servando.calendula.util.Snack;

//import es.usc.citius.servando.calendula.fragments.MedicineCreateOrEditFragment;

public class ScheduleCreationActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {

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

    int selectedPage = -1;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules);

        processIntent();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        //toolbar.setTitle(getString(R.string.title_activity_schedules));        
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((TextView) findViewById(R.id.textView2)).setText(getString(mScheduleId != -1 ? R.string.title_edit_schedule_activity : R.string.title_create_schedule_activity));
        toolbar.setNavigationIcon(new InsetDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp), 10, 10, 10, 10));
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(2);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        tabs.setOnPageChangeListener(this);
        tabs.setAllCaps(true);
        tabs.setShouldExpand(true);
        tabs.setDividerPadding(3);
        tabs.setDividerColor(getResources().getColor(R.color.white_50));
        tabs.setDividerColor(getResources().getColor(R.color.transparent));
        tabs.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.tab_indicator_height));
        tabs.setIndicatorColor(getResources().getColor(R.color.android_blue));
        tabs.setTextColor(getResources().getColor(R.color.android_blue));
        tabs.setUnderlineColor(getResources().getColor(R.color.android_blue_light));
        tabs.setViewPager(mViewPager);

        if (mSchedule != null) {
            mViewPager.setCurrentItem(1);
        }

        CalendulaApp.eventBus().register(this);

        // set first page indicator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
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
        if (mScheduleId != -1) {
            Schedule s = Schedule.findById(mScheduleId);
            if (s != null) {
                ScheduleCreationHelper.instance().setSelectedMed(s.medicine());
                ScheduleCreationHelper.instance().setSelectedDays(s.days());
                ScheduleCreationHelper.instance().setTimesPerDay(s.items().size());
                ScheduleCreationHelper.instance().setSelectedScheduleIdx(s.items().size() - 1);
                ScheduleCreationHelper.instance().setScheduleItems(s.items());
                mSchedule = s;
            } else {
                Snack.show("Schedule not found :(", this);
            }
        }

    }


    public void createSchedule() {
        try {

            final Schedule s = new es.usc.citius.servando.calendula.persistence.Schedule();

            TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // save schedule

                    s.setMedicine(ScheduleCreationHelper.instance().getSelectedMed());
                    s.setDays(ScheduleCreationHelper.instance().getSelectedDays());
                    s.save();

                    Log.d(TAG, "Saving schedule...");

                    for (ScheduleItem item : ScheduleCreationHelper.instance().getScheduleItems()) {
                        item.setSchedule(s);
                        item.save();
                        Log.d(TAG, "Saving item..." + item.getId());
                        // add to daily schedule
                        DailyScheduleItem dsi = new DailyScheduleItem(item);
                        dsi.save();
                        Log.d(TAG, "Saving daily schedule item..." + dsi.getId() + ", " + dsi.scheduleItem().getId());
                    }
                    // save and fire event
                    Persistence.instance().save(s);

                    return null;
                }
            });
            
            ScheduleCreationHelper.instance().clear();
            AlarmScheduler.instance().onCreateOrUpdateSchedule(s, ScheduleCreationActivity.this);
            Log.d(TAG, "Schedule saved successfully!");
            Snack.show(R.string.schedule_created_message, this);
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

                    s.setMedicine(ScheduleCreationHelper.instance().getSelectedMed());
                    s.setDays(ScheduleCreationHelper.instance().getSelectedDays());

                    List<Long> routinesTaken = new ArrayList<Long>();

                    for (ScheduleItem item : s.items()) {
                        DailyScheduleItem d = DailyScheduleItem.findByScheduleItem(item);
                        // if taken today, add to the list
                        if (d.takenToday()) {
                            routinesTaken.add(item.routine().getId());
                        }
                        item.deleteCascade();
                    }

                    // save new items
                    for (ScheduleItem i : ScheduleCreationHelper.instance().getScheduleItems()) {

                        ScheduleItem item = new ScheduleItem();
                        item.setDose(i.dose());
                        item.setRoutine(i.routine());
                        item.setSchedule(s);
                        item.save();
                        // add to daily schedule
                        DailyScheduleItem dsi = new DailyScheduleItem(item);
                        if (routinesTaken.contains(item.routine().getId())) {
                            dsi.setTakenToday(true);
                        }
                        dsi.save();
                    }
                    // save and fire event
                    Persistence.instance().save(s);
                    return null;
                }
            });
            
            ScheduleCreationHelper.instance().clear();
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

//        try {

        if (!validateBeforeSave()) {
            return;
        }

        if (mSchedule != null) {
            updateSchedule();
        } else {
            createSchedule();
        }

//            ActiveAndroid.beginTransaction();
//            ArrayList<Long> scheduleItemIds = new ArrayList<Long>();
//            Medicine m = ScheduleCreationHelper.instance().getSelectedMed();
//
//            Schedule s = mSchedule != null ? mSchedule : new es.usc.citius.servando.calendula.persistence.Schedule();
//            s.setMedicine(m);
//            s.setDays(ScheduleCreationHelper.instance().getSelectedDays());
//            s.save();
//
//            for (ScheduleItem item : ScheduleCreationHelper.instance().getScheduleItems()) {
//                item.setSchedule(s);
//                item.save();
//                scheduleItemIds.add(item.getId());
//                // for each item, add a new DailyScheduleItem item for it
//                if (DailyScheduleItem.findByScheduleItem(item) == null) {
//                    Log.d(TAG, "Creating daily schedule item for " + item.routine().name());
//                    new DailyScheduleItem(item).save();
//                } else {
//                    Log.d(TAG, "Not creating daily schedule item for " + item.routine().name());
//                }
//
//                Log.d(TAG, "Add item: " + s.getId() + ", " + item.getId());
//            }
//
//            for (ScheduleItem scheduleItem : s.items()) {
//                if (!ScheduleCreationHelper.instance().getScheduleItems().contains(scheduleItem)) {
//                    Log.d(TAG, "Item to remove : " + scheduleItem.getId() + ", " + scheduleItem.routine().name() + ", " + scheduleItem.dose());
//                    scheduleItem.deleteCascade();
//                }
//            }
//
//            Persistence.instance().save(s);
//            Log.d(TAG, "Schedule saved successfully!");
//            ActiveAndroid.setTransactionSuccessful();
//            AlarmScheduler.instance().onCreateOrUpdateSchedule(s, this);
//            ScheduleCreationHelper.instance().clear();
//            Snack.show(R.string.schedule_created_message, this);
//            // send result to caller activity
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra("schedule_created", true);
//            setResult(RESULT_OK, returnIntent);
//            finish();
//
//        } catch (Exception e) {
//            Snack.show("Error creating schedule",this);
//            e.printStackTrace();
//        } finally {
//            if(ActiveAndroid.inTransaction()) {
//                ActiveAndroid.endTransaction();
//            }
//        }
    }


    boolean validateBeforeSave() {

        if (ScheduleCreationHelper.instance().getSelectedMed() == null) {
            mViewPager.setCurrentItem(0);
            showSnackBar(R.string.create_schedule_unselected_med);
            return false;
        }

        for (ScheduleItem i : ScheduleCreationHelper.instance().getScheduleItems()) {
            if (i.routine() == null) {
                mViewPager.setCurrentItem(1);
                showSnackBar(R.string.create_schedule_incomplete_items);
                return false;
            }
        }

        for (ScheduleItem i : ScheduleCreationHelper.instance().getScheduleItems()) {
            if (i.dose() <= 0) {
                mViewPager.setCurrentItem(1);
                showSnackBar(R.string.create_schedule_incomplete_doses);
                return false;
            }
        }

        if (ScheduleCreationHelper.instance().getDays(this).length <= 0) {
            mViewPager.setCurrentItem(1);
            showSnackBar(R.string.schedule_no_day_specified_message);
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
//            case R.id.action_done:
//                saveSchedule();
//                return true;
            default:
                finish();
                return true;
        }
    }

    public void onMedicineSelected(Medicine m, boolean step) {

        ScheduleCreationHelper.instance().setSelectedMed(m);

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


    void showDeleteConfirmationDialog(final Schedule s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.remove_medicine_message_short), s.medicine().name()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Persistence.instance().deleteCascade(s);
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
        CalendulaApp.eventBus().unregister(this);
        ScheduleCreationHelper.instance().clear();
        super.onDestroy();
    }

//    @Override
//    public void onBackPressed() {
        /*
        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        } else {
            ScheduleCreationHelper.instance().clear();
            super.onBackPressed();
        }
        */
//    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
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
            if (position == 0) {
                return new SelectMedicineListFragment();
            } else if (position == 1) {
                return new ScheduleTimetableFragment();
            } else {
                return new ScheduleSummaryFragment();
            }
        }


        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Medicina";
            } else if (position == 1) {
                return " Pauta  ";
            } else {
                return "Resumen ";
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }


    public void onEvent(PersistenceEvents.MedicineAddedEvent event) {
        Log.d("onEvent", event.id + " ----");
        ((SelectMedicineListFragment) getViewPagerFragment(0)).setSelectedMed(event.id);
    }

}
