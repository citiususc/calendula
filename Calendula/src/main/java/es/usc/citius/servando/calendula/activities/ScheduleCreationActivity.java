package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;

import java.util.Locale;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.fragments.MedicineCreateOrEditFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleSummaryFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleTimetableFragment;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.ScheduleCreationHelper;

public class ScheduleCreationActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener, MedicineCreateOrEditFragment.OnMedicineEditListener {

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
    ActionBar mActionBar;

    Button prevButton;
    Button nextButton;

    int currentPageIndicatorColor;
    int normalPageIndicatorColor;
    int selectedPage = -1;

    // Medicine reference that will be created and returned by the createOrEdit fragment
    Medicine med;
    Schedule mSchedule;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    long mScheduleId;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules);

        processIntent();

        normalPageIndicatorColor = getResources().getColor(R.color.android_blue_light);
        currentPageIndicatorColor = getResources().getColor(R.color.android_blue_dark);

        prevButton = (Button) findViewById(R.id.schedules_prev_button);
        nextButton = (Button) findViewById(R.id.schedules_next_button);

        setFormOnClickListeners();

//        getSupportActionBar().hide();

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_launcher_white);
//        // configure toolbar as action bar
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        getSupportActionBar().setDisplayShowCustomEnabled(false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        // set first page indicator
        setCurrentPageIndicator(0);
    }

    private void processIntent() {
        mScheduleId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1);
        if (mScheduleId != -1) {
            Schedule s = Schedule.findById(mScheduleId);
            ScheduleCreationHelper.instance().setSelectedMed(s.medicine());
            ScheduleCreationHelper.instance().setSelectedDays(s.days());
            ScheduleCreationHelper.instance().setTimesPerDay(s.items().size());
            ScheduleCreationHelper.instance().setSelectedScheduleIdx(s.items().size() - 1);
            ScheduleCreationHelper.instance().setScheduleItems(s.items());
            mSchedule = s;
        }
    }


    public void saveSchedule() {

        try {
            ActiveAndroid.beginTransaction();

            Medicine m = ScheduleCreationHelper.instance().getSelectedMed();
            m.save();

            Schedule s = mSchedule != null ? mSchedule : new es.usc.citius.servando.calendula.persistence.Schedule();
            s.setMedicine(m);
            s.setDays(ScheduleCreationHelper.instance().getSelectedDays());
            s.save();

            for (ScheduleItem item : ScheduleCreationHelper.instance().getScheduleItems()) {
                item.setSchedule(s);
                item.save();
                // for each item, add a new DailyScheduleItem item for it
                new DailyScheduleItem(item).save();
                Log.d(TAG, "Add item: " + s.getId() + ", " + item.getId());
            }

            Log.d(TAG, "Schedule saved successfully!");
            ActiveAndroid.setTransactionSuccessful();

            AlarmScheduler.instance().setAlarmsIfNeeded(s, getBaseContext());
            ScheduleCreationHelper.instance().clear();
            Toast.makeText(ScheduleCreationActivity.this, "Schedule created!", Toast.LENGTH_LONG).show();

            // send result to caller activity
            Intent returnIntent = new Intent();
            returnIntent.putExtra("schedule_created", true);
            setResult(RESULT_OK, returnIntent);
            finish();


            finish();

        } catch (Exception e) {
            Toast.makeText(this, " Error creating schedule", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    void setFormOnClickListeners() {
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show prev page if any
                int currentPage = mViewPager.getCurrentItem();
                if (currentPage > 0) {
                    mViewPager.setCurrentItem(currentPage - 1);
                }
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show next page if any
                int currentPage = mViewPager.getCurrentItem();
                if (currentPage < 2 && validatePage(currentPage)) {
                    mViewPager.setCurrentItem(currentPage + 1);
                } else if (currentPage == 2) {
                    saveSchedule();
                }
            }
        });
    }


    boolean validatePage(int page) {
        if (page == 0) {
            MedicineCreateOrEditFragment fragment = ((MedicineCreateOrEditFragment) getViewPagerFragment(0));
            if (fragment.validate()) {
                med = fragment.getMedicineFromView();
                ScheduleCreationHelper.instance().setSelectedMed(med);
                Log.d(ScheduleCreationActivity.class.getName(), "Med created but no saved: " + med.name() + ", " + med.presentation().getName(getResources()));
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.schedules, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    void setCurrentPageIndicator(int page) {

        Log.d(ScheduleCreationActivity.class.getName(), "page: " + page + ", current: " + selectedPage);

        if (page != selectedPage) {
            // uncheck old page indicator
            if (selectedPage == 0) {
                findViewById(R.id.add_sched_page_indicator_up_1).setBackgroundColor(normalPageIndicatorColor);
                findViewById(R.id.add_sched_page_indicator_down_1).setBackgroundColor(normalPageIndicatorColor);
                Log.d(ScheduleCreationActivity.class.getName(), "Set background");
            } else if (selectedPage == 1) {
                findViewById(R.id.add_sched_page_indicator_up_2).setBackgroundColor(normalPageIndicatorColor);
                findViewById(R.id.add_sched_page_indicator_down_2).setBackgroundColor(normalPageIndicatorColor);
            } else if (selectedPage == 2) {
                findViewById(R.id.add_sched_page_indicator_up_3).setBackgroundColor(normalPageIndicatorColor);
                findViewById(R.id.add_sched_page_indicator_down_3).setBackgroundColor(normalPageIndicatorColor);
            }
            // check new page indicator
            if (page == 0) {
                findViewById(R.id.add_sched_page_indicator_up_1).setBackgroundColor(currentPageIndicatorColor);
                findViewById(R.id.add_sched_page_indicator_down_1).setBackgroundColor(currentPageIndicatorColor);
                // update buttons
                prevButton.setBackgroundResource(R.drawable.transparent_button_selector);
                prevButton.setEnabled(false);
                nextButton.setBackgroundResource(R.drawable.next_button_selector);
                nextButton.setText(R.string.next);

            } else if (page == 1) {
                findViewById(R.id.add_sched_page_indicator_up_2).setBackgroundColor(currentPageIndicatorColor);
                findViewById(R.id.add_sched_page_indicator_down_2).setBackgroundColor(currentPageIndicatorColor);
                // update buttons
                prevButton.setEnabled(true);
                prevButton.setBackgroundResource(R.drawable.prev_button_selector);
                nextButton.setBackgroundResource(R.drawable.next_button_selector);
                nextButton.setText(R.string.next);

            } else if (page == 2) {
                findViewById(R.id.add_sched_page_indicator_up_3).setBackgroundColor(currentPageIndicatorColor);
                findViewById(R.id.add_sched_page_indicator_down_3).setBackgroundColor(currentPageIndicatorColor);
                // update buttons
                prevButton.setEnabled(true);
                prevButton.setBackgroundResource(R.drawable.prev_button_selector);
                nextButton.setBackgroundResource(R.drawable.confirm_button_selector);
                nextButton.setText(R.string.confirm);
            }

            selectedPage = page;

            Log.d(ScheduleCreationActivity.class.getName(), "page: " + page + ", current: " + selectedPage);
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int page) {
        setCurrentPageIndicator(page);
        // if we are going to show the summary page
        // update it
        if (page == 2) {

        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onMedicineEdited(Medicine r) {

    }

    @Override
    public void onMedicineCreated(Medicine m) {
        // save med reference
        med = m;
        m.save();
        // go to next step
        mViewPager.setCurrentItem(1);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return new MedicineCreateOrEditFragment();
            } else if (position == 1) {
                return new ScheduleTimetableFragment();
            } else {
                return new ScheduleSummaryFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
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
