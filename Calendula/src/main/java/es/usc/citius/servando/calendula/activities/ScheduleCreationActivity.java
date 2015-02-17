package es.usc.citius.servando.calendula.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
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
        getSupportActionBar().setTitle(getString(mScheduleId != -1 ? R.string.title_edit_schedule_activity : R.string.title_create_schedule_activity));
        toolbar.setNavigationIcon(new InsetDrawable(getResources().getDrawable(R.drawable.ic_event_white_48dp), 18, 18, 18, 18));
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(2);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        tabs.setOnPageChangeListener(this);
        tabs.setAllCaps(true);
        tabs.setShouldExpand(true);
        tabs.setDividerColor(getResources().getColor(R.color.white_50));
        tabs.setDividerColor(getResources().getColor(R.color.transparent));
        tabs.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.tab_indicator_height));
        tabs.setIndicatorColor(getResources().getColor(R.color.white));
        tabs.setTextColor(getResources().getColor(R.color.white));
        tabs.setUnderlineColor(getResources().getColor(R.color.transparent));
        tabs.setViewPager(mViewPager);

        if (mSchedule != null) {
            mViewPager.setCurrentItem(1);
        }
        
        // set first page indicator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
        }
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
                Toast.makeText(this, "Schedule not found :(", Toast.LENGTH_SHORT).show();
            }
        }

    }


    public void saveSchedule() {

        try {
            ActiveAndroid.beginTransaction();

            ArrayList<Long> scheduleItemIds = new ArrayList<Long>();

            Medicine m = ScheduleCreationHelper.instance().getSelectedMed();

            Schedule s = mSchedule != null ? mSchedule : new es.usc.citius.servando.calendula.persistence.Schedule();
            s.setMedicine(m);
            s.setDays(ScheduleCreationHelper.instance().getSelectedDays());
            s.save();

            for (ScheduleItem item : ScheduleCreationHelper.instance().getScheduleItems()) {
                item.setSchedule(s);
                item.save();
                scheduleItemIds.add(item.getId());
                // for each item, add a new DailyScheduleItem item for it
                if (DailyScheduleItem.findByScheduleItem(item) == null) {
                    Log.d(TAG, "Creating daily schedule item for " + item.routine().name());
                    new DailyScheduleItem(item).save();
                } else {
                    Log.d(TAG, "Not creating daily schedule item for " + item.routine().name());
                }

                Log.d(TAG, "Add item: " + s.getId() + ", " + item.getId());
            }

            for (ScheduleItem scheduleItem : s.items()) {
                if (!ScheduleCreationHelper.instance().getScheduleItems().contains(scheduleItem)) {
                    Log.d(TAG, "Item to remove : " + scheduleItem.getId() + ", " + scheduleItem.routine().name() + ", " + scheduleItem.dose());
                    scheduleItem.deleteCascade();
                }
            }

            Persistence.instance().save(s);
            Log.d(TAG, "Schedule saved successfully!");
            ActiveAndroid.setTransactionSuccessful();
            AlarmScheduler.instance().onCreateOrUpdateSchedule(s, this);
            ScheduleCreationHelper.instance().clear();
            Toast.makeText(ScheduleCreationActivity.this, getString(R.string.schedule_created_message), Toast.LENGTH_LONG).show();
            // send result to caller activity
            Intent returnIntent = new Intent();
            returnIntent.putExtra("schedule_created", true);
            setResult(RESULT_OK, returnIntent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, " Error creating schedule", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }


    boolean validatePage(int page) {

        if (page == 1) {

            for (ScheduleItem i : ScheduleCreationHelper.instance().getScheduleItems()) {
                if (i.routine() == null) {
                    Toast.makeText(this, R.string.create_schedule_incomplete_items, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            if (ScheduleCreationHelper.instance().getDays(this).length <= 0) {
                Toast.makeText(this, getString(R.string.schedule_no_day_specified_message), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.medicines, menu);
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
            case R.id.action_done:
                saveSchedule();
                return true;
            default:
                return true;
        }
    }

    public void onMedicineSelected(Medicine m) {
        if (!autoStepDone) {
            ScheduleCreationHelper.instance().setSelectedMed(m);
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

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        } else {
            ScheduleCreationHelper.instance().clear();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        ScheduleCreationHelper.instance().clear();
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

}
