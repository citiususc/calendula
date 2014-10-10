package es.usc.citius.servando.calendula.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.fragments.RoutineCreateOrEditFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.FragmentUtils;

public class RoutinesActivity extends ActionBarActivity implements RoutinesListFragment.OnRoutineSelectedListener, RoutineCreateOrEditFragment.OnRoutineEditListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
//    RoutinesListFragment listFragment;
//    RoutineCreateOrEditFragment editFragment;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    String listFragmentName;
    String editFragmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routines);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        listFragmentName = FragmentUtils.makeViewPagerFragmentName(R.id.pager, 0);
        editFragmentName = FragmentUtils.makeViewPagerFragmentName(R.id.pager, 1);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.routines, menu);
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

    @Override
    public void onRoutineSelected(Routine r) {
        mViewPager.setCurrentItem(1);
        ((RoutineCreateOrEditFragment) getViewPagerFragment(1)).setRoutine(r);
        setTitle(R.string.title_edit_routine_activity);

    }

    @Override
    public void onCreateRoutine() {
        mViewPager.setCurrentItem(1);
        ((RoutineCreateOrEditFragment) getViewPagerFragment(1)).clear();
        setTitle(R.string.title_create_routine_activity);
    }

    @Override
    public void onRoutineEdited(Routine r) {
        AlarmScheduler.instance().setAlarm(r, this);
        Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
        mViewPager.setCurrentItem(0);
        ((RoutinesListFragment) getViewPagerFragment(0)).notifyDataChange();
        setTitle(R.string.title_activity_routines);
    }

    @Override
    public void onRoutineCreated(Routine r) {
        Toast.makeText(this, "Routine created!", Toast.LENGTH_SHORT).show();
        mViewPager.setCurrentItem(0);
        ((RoutinesListFragment) getViewPagerFragment(0)).notifyDataChange();
        setTitle(R.string.title_activity_routines);
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
                return new RoutinesListFragment();
            } else {
                return new RoutineCreateOrEditFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
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
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0);
            setTitle(R.string.title_activity_routines);
        } else {
            super.onBackPressed();
        }
    }


    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

}
