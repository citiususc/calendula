package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.util.FragmentUtils;

public class SchedulesActivity extends ActionBarActivity implements ScheduleListFragment.OnScheduleSelectedListener {

    private static final String TAG = SchedulesActivity.class.getSimpleName();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
//    RoutinesListFragment listFragment;
//    RoutineCreateOrEditFragment editFragment;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    String listFragmentName;
    String editFragmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        listFragmentName = FragmentUtils.makeViewPagerFragmentName(R.id.pager, 0);
        editFragmentName = FragmentUtils.makeViewPagerFragmentName(R.id.pager, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resume schedules activity");

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
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
            case R.id.action_remove_all:
                //TODO: remove all
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScheduleSelected(Schedule r) {

    }

    @Override
    public void onCreateSchedule() {
        Intent intent = new Intent(this, ScheduleCreationActivity.class);
        startActivityForResult(intent, 1);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                boolean scheduleCreated = data.getBooleanExtra("schedule_created", false);
                if (scheduleCreated) {
//                    ((ScheduleListFragment) getViewPagerFragment(0)).notifyDataChange();
                }
            }
        }
    }//onActivityResult


    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
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

            return new ScheduleListFragment();
        }

        @Override
        public int getCount() {
            // Show 1 total pages.
            return 1;
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0);
            setTitle(R.string.title_activity_schedule_list);
        } else {
            super.onBackPressed();
        }
    }


    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    private void launchActivity(Class activityCls) {
        Intent intent = new Intent(this, activityCls);
        startActivity(intent);
    }

}
